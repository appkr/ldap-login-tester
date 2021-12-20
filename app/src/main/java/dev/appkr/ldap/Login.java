package dev.appkr.ldap;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.util.concurrent.Callable;

@Command(
    name = "login",
    description = "login to the ldap server"
)
public class Login implements Callable<Integer> {

  @ParentCommand
  private App parent;

  @Override
  public Integer call() throws Exception {
    try {
      parent.validate();
      final LdapOperations ldap = new LdapOperations(parent.host, parent.port, parent.username, String.valueOf(parent.password));
      final DirContext ctx = ldap.getCtx();
      System.out.println("SUCCESS: " + ctx.getAttributes(parent.base));
    } catch (Exception e) {
      System.out.println();
      System.out.println("ERROR: " + e.getMessage());
      return 1;
    }

    return 0;
  }
}
