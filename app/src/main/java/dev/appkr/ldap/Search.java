package dev.appkr.ldap;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import javax.naming.directory.SearchResult;
import java.util.concurrent.Callable;

@Command(
    name = "search",
    description = "search a user from the ldap server"
)
public class Search implements Callable<Integer> {

  @ParentCommand
  private App parent;

  @Override
  public Integer call() throws Exception {
    try {
      parent.validate();

      final LdapOperations ldap = new LdapOperations(parent.host, parent.port, parent.username, String.valueOf(parent.password));

      //1) lookup the ldap account
      SearchResult srLdapUser = ldap.findAccountByAccountName(parent.base, parent.username);
      System.out.println("NAME: "  + srLdapUser.getName());

      //2) get the SID of the users primary group
      String primaryGroupSID = ldap.getPrimaryGroupSID(srLdapUser);
      System.out.println("GROUP SID: " + primaryGroupSID);

      //3) get the users Primary Group
      String primaryGroupName = ldap.findGroupBySID(parent.base, primaryGroupSID);
    } catch (Exception e) {
      System.out.println();
      System.out.println("ERROR: " + e.getMessage());
      return 1;
    }

    return 0;
  }
}
