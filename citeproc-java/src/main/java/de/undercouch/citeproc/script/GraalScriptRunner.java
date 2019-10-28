// Copyright 2013-2019 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.undercouch.citeproc.script;

import de.undercouch.citeproc.VariableWrapper;
import de.undercouch.citeproc.VariableWrapperParams;
import de.undercouch.citeproc.helper.json.JsonBuilder;
import de.undercouch.citeproc.helper.json.JsonObject;
import de.undercouch.citeproc.helper.json.StringJsonBuilder;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Executes JavaScript scripts through GraalVM JavaScript
 * @author Michel Kraemer
 */
public class GraalScriptRunner extends AbstractScriptRunner {
    private Context ctx = Context.newBuilder("js").allowAllAccess(true).build();

    @Override
    public String getName() {
        return ctx.getEngine().getImplementationName();
    }

    @Override
    public String getVersion() {
        return ctx.getEngine().getVersion();
    }

    @Override
    public void eval(Reader reader) throws IOException {
        ctx.eval(Source.newBuilder("js", reader, null).cached(false).build());
    }

    @Override
    public <T> T callMethod(String name, Class<T> resultType, Object... args)
            throws ScriptRunnerException {
        try {
            return convert(ctx.getBindings("js").getMember(name)
                    .execute(convertArguments(args)), resultType);
        } catch (IOException | PolyglotException e) {
            throw new ScriptRunnerException("Could not call method", e);
        }
    }

    @Override
    public void callMethod(String name, Object... args) throws ScriptRunnerException {
        try {
            ctx.getBindings("js").getMember(name).executeVoid(convertArguments(args));
        } catch (IOException | PolyglotException e) {
            throw new ScriptRunnerException("Could not call method", e);
        }
    }

    @Override
    public <T> T callMethod(Object obj, String name, Class<T> resultType, Object... args)
            throws ScriptRunnerException {
        try {
            return convert(((Value)obj).getMember(name)
                    .execute(convertArguments(args)), resultType);
        } catch (IOException | PolyglotException e) {
            throw new ScriptRunnerException("Could not call method", e);
        }
    }

    @Override
    public void callMethod(Object obj, String name, Object... args)
            throws ScriptRunnerException {
        try {
            ((Value)obj).getMember(name).executeVoid(convertArguments(args));
        } catch (IOException | PolyglotException e) {
            throw new ScriptRunnerException("Could not call method", e);
        }
    }

    /**
     * Recursively convert a JavaScript value to a Java object
     * @param v the value to convert
     * @return the object
     */
    private static Object convert(Value v) {
        Object o = v;
        if (v.isNull()) {
            o = null;
        } else if (v.isBoolean()) {
            o = v.asBoolean();
        } else if (v.isDate()) {
            o = v.asDate();
        } else if (v.isDuration()) {
            o = v.asDuration();
        } else if (v.isHostObject()) {
            o = v.asHostObject();
        } else if (v.isInstant()) {
            o = v.asInstant();
        } else if (v.isNativePointer()) {
            o = v.asNativePointer();
        } else if (v.isNumber()) {
            if (v.fitsInInt()) {
                o = v.asInt();
            } else if (v.fitsInLong()) {
                o = v.asLong();
            } else if (v.fitsInDouble()) {
                o = v.asDouble();
            } else {
                throw new IllegalStateException("Unknown type of number");
            }
        } else if (v.isProxyObject()) {
            o = v.asProxyObject();
        } else if (v.isString()) {
            o = v.asString();
        } else if (v.isTime()) {
            o = v.asTime();
        } else if (v.isTimeZone()) {
            o = v.asTimeZone();
        } else if (v.hasArrayElements()) {
            o = convertArray(v);
        } else if (v.hasMembers()) {
            o = convertObject(v);
        }
        return o;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T convert(Object o, Class<T> type) {
        if (type != Object.class && o instanceof Value) {
            o = convert((Value)o);
        }
        return (T)o;
    }

    /**
     * Recursively convert a JavaScript array to a list
     * @param arr the array to convert
     * @return the list
     */
    private static List<Object> convertArray(Value arr) {
        List<Object> l = new ArrayList<>();
        for (int i = 0; i < arr.getArraySize(); ++i) {
            Value v = arr.getArrayElement(i);
            Object o = convert(v);
            l.add(o);
        }
        return l;
    }

    /**
     * Recursively convert a JavaScript object to a map
     * @param obj the object to convert
     * @return the map
     */
    private static Map<String, Object> convertObject(Value obj) {
        Map<String, Object> r = new LinkedHashMap<>();
        for (String k : obj.getMemberKeys()) {
            Value v = obj.getMember(k);
            Object o = convert(v);
            r.put(k, o);
        }
        return r;
    }

    /**
     * Convert arguments that should be passed to a JavaScript function
     * @param args the arguments
     * @return the converted arguments or `args` if conversion was not necessary
     * @throws IOException if an argument could not be converted
     */
    private Object[] convertArguments(Object[] args) throws IOException {
        Object[] copy = args;
        for (int i = 0; i < args.length; ++i) {
            Object v = args[i];
            Object o = v;
            if (v instanceof JsonObject || v instanceof Collection ||
                    (v != null && v.getClass().isArray()) || v instanceof Map) {
                String so = createJsonBuilder().toJson(v).toString();
                Source src = Source.newBuilder("js", "(" + so + ")", "parseMyJSON")
                        .cached(false) // we'll most likely never convert the same object again
                        .build();
                o = ctx.eval(src);
            } else if (v instanceof VariableWrapper) {
                o = new VariableWrapperWrapper((VariableWrapper)o);
            }
            if (o != v) {
                if (copy == args) {
                    copy = Arrays.copyOf(args, args.length);
                }
                copy[i] = o;
            }
        }
        return copy;
    }

    @Override
    public void release(Object o) {
        // nothing to do here
    }

    @Override
    public JsonBuilder createJsonBuilder() {
        return new StringJsonBuilder(this);
    }

    @Override
    public void close() {
        ctx.close();
    }

    /**
     * Wraps around {@link VariableWrapper} and converts
     * {@link VariableWrapperParams} objects to JSON objects. This class is
     * public because Graal JavaScript needs to be able to access it.
     * @author Michel Kraemer
     */
    public static class VariableWrapperWrapper {
        private final VariableWrapper wrapper;

        private VariableWrapperWrapper(VariableWrapper wrapper) {
            this.wrapper = wrapper;
        }

        /**
         * Call the {@link VariableWrapper} with the given parameters
         * @param params the context in which an item should be rendered
         * @param prePunct the text that precedes the item to render
         * @param str the item to render
         * @param postPunct the text that follows the item to render
         * @return the string to be rendered
         */
        @SuppressWarnings("unused")
        public String wrap(Value params, String prePunct,
                String str, String postPunct) {
            Map<String, Object> m = convertObject(params);
            VariableWrapperParams p = VariableWrapperParams.fromJson(m);
            return wrapper.wrap(p, prePunct, str, postPunct);
        }
    }
}
