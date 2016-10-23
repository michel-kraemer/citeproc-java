---
layout: default
title: Remote connectors
prev: using/importers/
prevtitle: Importers
next: using/output-formats/
nexttitle: Output formats
---

citeproc-java contains remote connectors that allow you to read documents from
[Mendeley Web](http://www.mendeley.com) or [Zotero](https://www.zotero.org)
and to use them as input for citations or bibliographies.

Mendeley uses [OAuth 2.0](https://en.wikipedia.org/wiki/OAuth) authorization.
You have to obtain a consumer key and consumer secret for
your application from the [Mendeley Developers Portal](http://dev.mendeley.com/).
Additionally, your application needs to provide a web page that your
users will be redirected to after they have entered their credentials.
You can create a Mendeley remote connector as follows:

{% highlight java %}
import de.undercouch.citeproc.mendeley.MendeleyConnector;
import de.undercouch.citeproc.remote.RemoteConnector;

String consumerKey = "ENTER YOUR CONSUMER KEY HERE";
String consumerSecret = "ENTER YOUR CONSUMER SECRET HERE";
String redirectUrl = "ENTER YOUR REDIRECT URL HERE";

RemoteConnector rc = new MendeleyConnector(consumerKey, consumerSecret,
    redirectUrl);
{% endhighlight %}

Zotero uses OAuth as well. Register your application with
[Zotero](https://www.zotero.org/oauth/apps) to get a consumer key and
a consumer secret. After that you can create a Zotero remote connector
as follows:

{% highlight java %}
import de.undercouch.citeproc.remote.RemoteConnector;
import de.undercouch.citeproc.zotero.ZoteroConnector;

String consumerKey = "ENTER YOUR CONSUMER KEY HERE";
String consumerSecret = "ENTER YOUR CONSUMER SECRET HERE";

RemoteConnector rc = new ZoteroConnector(consumerKey, consumerSecret);
{% endhighlight %}

The next step is to ask users to authorize your application to
access their remote account. Use an `AuthenticatedRemoteConnector`
to cache the authorization tokens, so the users do not have to
go through the authorization process over and over again.

{% highlight java %}
import de.undercouch.citeproc.helper.oauth.AuthenticationStore;
import de.undercouch.citeproc.helper.oauth.FileAuthenticationStore;
import de.undercouch.citeproc.remote.AuthenticatedRemoteConnector;

//configure authentication store
File configDir = new File(System.getProperty("user.home"), ".yourapp");
configDir.mkdirs();
File authStoreFile = new File(configDir, "your-auth-store.conf");
AuthenticationStore authStore = new FileAuthenticationStore(authStoreFile);
rc = new AuthenticatedRemoteConnector(rc, authStore);
{% endhighlight %}

The best way to check if the tool is already authorized is by just
performing a request and catching the exception.

{% highlight java %}
import de.undercouch.citeproc.helper.oauth.UnauthorizedException;

int retries = 1;
while (true) {
  try {
    List<String> itemIds = rc.getItemIDs();
    // ...
    break;
  } catch (UnauthorizedException e) {
    if (retries == 0) {
      //too may retries
      throw new IllegalStateException("Authorization failed.");
    }
    --retries;
    
    //get authorization URL
    String authUrl = rc.getAuthorizationURL();
    
    System.out.println("This tool requires authorization. Please point your "
      + "web browser to the\nfollowing URL and follow the instructions:\n");
    System.out.println(authUrl);
    System.out.println();
    
    //read verification code from console
    System.out.print("Enter verification code: ");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String verificationCode = br.readLine();
    
    //authorize...
    rc.authorize(verificationCode);
    
    //retry
    continue;
  }
}
{% endhighlight %}

Finally, you can use the `RemoteConnector` to read the list of
items from the server and to obtain details for each of them.

{% highlight java %}
import de.undercouch.citeproc.csl.CSLItemData;

//read list of Mendeley document IDs
List<String> ids = rc.getItemIDs();

//read details for each document
List<CSLItemData> items = new ArrayList<CSLItemData>();
for (String id : ids) {
  CSLItemData item = rc.getItem(id);
  items.add(item);
}
{% endhighlight %}

Put the read items into a `ListItemDataProvider` to use them
as input for the CSL processor:

{% highlight java %}
import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.ListItemDataProvider;

ItemDataProvider provider = new ListItemDataProvider(items);
CSL citeproc = new CSL(provider, "ieee");
//...
{% endhighlight %}
