---
layout: default
title: Mendeley connector
prev: using/bibtex-converter/
prevtitle: BibTeX converter
next: using/output-formats/
nexttitle: Output formats
---

citeproc-java contains a Mendeley connector that allows you to read
documents from Mendeley Web and to use them as input for citations
or bibliographies.

Mendeley uses [OAuth](https://en.wikipedia.org/wiki/OAuth) authorization.
You have to obtain a consumer key and consumer secret for
your application from the [Mendeley Developers Portal](http://dev.mendeley.com/).
After that you can create a new Mendeley connector:

{% highlight java %}
import de.undercouch.citeproc.mendeley.DefaultMendeleyConnector;
import de.undercouch.citeproc.mendeley.MendeleyConnector;

String consumerKey = "ENTER YOUR CONSUMER KEY HERE";
String consumerSecret = "ENTER YOUR CONSUMER SECRET HERE";

MendeleyConnector mc = new DefaultMendeleyConnector(consumerKey, consumerSecret);
{% endhighlight %}

The next step is to ask users to authorize your application to
access their Mendeley Web account. Use an `AuthenticatedMendeleyConnector`
to cache the authorization tokens, so the users do not have to
go through the authorization process over and over again.

{% highlight java %}
import de.undercouch.citeproc.helper.oauth.AuthenticationStore;
import de.undercouch.citeproc.helper.oauth.FileAuthenticationStore;
import de.undercouch.citeproc.mendeley.AuthenticatedMendeleyConnector;

//configure authentication store
File configDir = new File(System.getProperty("user.home"), ".yourapp");
configDir.mkdirs();
File authStoreFile = new File(configDir, "mendeley-auth-store.conf");
AuthenticationStore authStore = new FileAuthenticationStore(authStoreFile);
mc = new AuthenticatedMendeleyConnector(mc, authStore);
{% endhighlight %}

The best way to check if the tool is already authorized is by just
performing a request and catching the exception.

{% highlight java %}
import de.undercouch.citeproc.helper.oauth.UnauthorizedException;

int retries = 1;
while (true) {
  try {
    List<String> docs = mc.getDocuments();
    // ...
    break;
  } catch (UnauthorizedException e) {
    if (retries == 0) {
      //too may retries
      throw new IllegalStateException("Authorization failed.");
    }
    --retries;
    
    //get authorization URL
    String authUrl = mc.getAuthorizationURL();
    
    System.out.println("This tool requires authorization. Please point your "
      + "web browser to the\nfollowing URL and follow the instructions:\n");
    System.out.println(authUrl);
    System.out.println();
    
    //read verification code from console
    System.out.print("Enter verification code: ");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String verificationCode = br.readLine();
    
    //authorize...
    mc.authorize(verificationCode);
    
    //retry
    continue;
  }
}
{% endhighlight %}

Finally, you can use the `MendeleyConnector` to read the list of
documents from the server and to obtain details for each document.

{% highlight java %}
import de.undercouch.citeproc.csl.CSLItemData;

//read list of Mendeley document IDs
List<String> ids = mc.getDocuments();

//read details for each document
List<CSLItemData> items = new ArrayList<CSLItemData>();
for (String id : ids) {
  CSLItemData item = mc.getDocument(id);
  items.add(item);
}
{% endhighlight %}

Put the read documents into a `ListItemDataProvider` to use them
as input for the CSL processor:

{% highlight java %}
import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.ListItemDataProvider;

ItemDataProvider provider = new ListItemDataProvider(items);
CSL citeproc = new CSL(provider, "ieee");
//...
{% endhighlight %}
