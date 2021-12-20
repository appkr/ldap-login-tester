// source: http://www.adamretter.org.uk/blog/entries/LDAPTest.java
package dev.appkr.ldap;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.net.InetAddress;
import java.util.Hashtable;

public class LdapOperations {

  private DirContext ctx;

  public LdapOperations(InetAddress host, Integer port, String username, String password) throws NamingException {
    Hashtable<String, Object> env = new Hashtable<>();
    env.put(Context.PROVIDER_URL, String.format("ldap://%s:%d", host.getHostAddress(), port));
    env.put(Context.SECURITY_PRINCIPAL, username);
    env.put(Context.SECURITY_CREDENTIALS, password);
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put("java.naming.ldap.attributes.binary", "objectSID");
    env.put("com.sun.jndi.ldap.trace.ber", System.err);
    this.ctx = new InitialDirContext(env);
  }

  public SearchResult findAccountByAccountName(String base, String username) throws NamingException {
    final String[] part = username.split("@");
    if (part.length != 2 || part[0].isEmpty()) {
      throw new IllegalArgumentException("cannot extract username from an email");
    }

    String searchFilter = "(&(objectClass=user)(sAMAccountName=" + part[0] + "))";
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    NamingEnumeration<SearchResult> results = ctx.search(base, searchFilter, searchControls);
    SearchResult searchResult = null;
    if (results.hasMoreElements()) {
      searchResult = (SearchResult) results.nextElement();

      //make sure there is not another item available, there should be only 1 match
      if (results.hasMoreElements()) {
        System.err.println("Matched multiple users for the accountName: " + username);
        return null;
      }
    }

    return searchResult;
  }

  public String findGroupBySID(String base, String sid) throws NamingException {
    String searchFilter = "(&(objectClass=group)(objectSid=" + sid + "))";
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    NamingEnumeration<SearchResult> results = ctx.search(base, searchFilter, searchControls);
    if (results.hasMoreElements()) {
      SearchResult searchResult = (SearchResult) results.nextElement();

      //make sure there is not another item available, there should be only 1 match
      if (results.hasMoreElements()) {
        System.err.println("Matched multiple groups for the group with SID: " + sid);
        return null;
      } else {
        return (String) searchResult.getAttributes().get("sAMAccountName").get();
      }
    }

    return null;
  }

  public String getPrimaryGroupSID(SearchResult srLdapUser) throws NamingException {
    byte[] objectSID = (byte[]) srLdapUser.getAttributes().get("objectSid").get();
    String strPrimaryGroupID = (String) srLdapUser.getAttributes().get("primaryGroupID").get();
    String strObjectSid = decodeSID(objectSID);

    return strObjectSid.substring(0, strObjectSid.lastIndexOf('-') + 1) + strPrimaryGroupID;
  }

  /**
   * The binary data is in the form: byte[0] - revision level byte[1] - count of sub-authorities byte[2-7] - 48 bit
   * authority (big-endian) and then count x 32 bit sub authorities (little-endian)
   * <p>
   * The String value is: S-Revision-Authority-SubAuthority[n]...
   * <p>
   * Based on code from here - http://forums.oracle.com/forums/thread.jspa?threadID=1155740&tstart=0
   */
  public String decodeSID(byte[] sid) {
    final StringBuilder strSid = new StringBuilder("S-");

    // get version
    final int revision = sid[0];
    strSid.append(Integer.toString(revision));

    //next byte is the count of sub-authorities
    final int countSubAuths = sid[1] & 0xFF;

    //get the authority
    long authority = 0;
    //String rid = "";
    for (int i = 2; i <= 7; i++) {
      authority |= ((long) sid[i]) << (8 * (5 - (i - 2)));
    }

    strSid.append("-");
    strSid.append(Long.toHexString(authority));

    //iterate all the sub-auths
    int offset = 8;
    int size = 4; //4 bytes for each sub auth
    for (int j = 0; j < countSubAuths; j++) {
      long subAuthority = 0;
      for (int k = 0; k < size; k++) {
        subAuthority |= (long) (sid[offset + k] & 0xFF) << (8 * k);
      }

      strSid.append("-");
      strSid.append(subAuthority);

      offset += size;
    }

    return strSid.toString();
  }

  public DirContext getCtx() {
    return ctx;
  }
}
