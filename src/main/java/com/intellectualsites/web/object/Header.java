// TODO Apply some real stuff
public class Header {
  
  private Map<String, String> headers;
  private String status;
  
  public Header(final String status) {
    this.status = status;
    this.headers = new HashMap<String, String>();
  }
  
  public void set(final String key, final String value) {
    this.headers.put(key, value);
  }
  
  public void apply(final PrintWriter out) {
    // We need to show that we are sending real content! :D
    out.println("HTTP/1.1 " + this.status);
    for (final Map.Entry<String, String> entry : this.headers) {
      out.println(entry.getKey() + ": " + this.getValue());
    }
    // Print one empty line to indicate that the header sending is finished, this is important as the content would otherwise
    // be classed as headers, which really isn't optimal <3
    out.println();
  }
  
  public String[] dump() {
    String[] dump = new String[this.headers.size() + 1];
    dump[0] = "HTTP/1.1 " + this.status;
    
    int index = 1;
    for (final Map.Entry<String, String> entry : this.headers) {
      dump[index++] = entry.getKey() + ": " + entry.getValue();
    }
    
    return dump;
  }
  
  public void redirect(final String newURL) {
    set("Location", newURL);
    set("status", "301");
}

  public void setCookie(final String cookie, final String value) {
      String value = "";
      if (this.headers.containsKey("Set-Cookie") {
          value = this.headers.get("Set-Cookie") + "," + cookie + "=" + value;
      } else {
          value = cookie + "=" + value; 
      }
      set("Set-Cookie", value);
  }
  
  public void removeCookie(final String cookie){
      setCookie(cookie, "deleted; expires=Thu, 01 Jan 1970 00:00:00 GMT");
  }
}
