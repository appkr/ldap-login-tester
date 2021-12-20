package dev.appkr.ldap;

import picocli.CommandLine;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.util.Arrays.asList;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

@Command(
    name = "ldap",
    mixinStandardHelpOptions = true, version = "ldap 1.0",
    description = "Test ldap functions",
    subcommands = { Login.class, Search.class })
public class App implements Callable<Integer> {

  @Option(names = {"-H", "--host"},
          description = "DNS or IP address of the ldap server",
          required = false,
          defaultValue = "10.161.11.84",
          scope = CommandLine.ScopeType.INHERIT)
  InetAddress host;

  @Option(names = {"-P", "--port"},
          description = "port number of the ldap server",
          required = false,
          defaultValue = "389",
          scope = CommandLine.ScopeType.INHERIT)
  Integer port;

  @Option(names = {"-u", "--username"},
          description = "username of the ldap",
          required = true,
          scope = CommandLine.ScopeType.INHERIT)
  String username;

  @Option(names = {"-p", "--password"},
          description = "passphrase to the ldap",
          required = true,
          interactive = true,
          scope = CommandLine.ScopeType.INHERIT)
  char[] password;

  @Option(names = {"-b", "--base"},
          description = "serach base value",
          required = false,
          defaultValue = "OU=Users,OU=MESH,DC=meshkorea,DC=net",
          scope = CommandLine.ScopeType.INHERIT)
  String base;

  public static void main(String[] args) throws Exception {
    int exitCode = new CommandLine(new App()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() throws Exception {
    return 0;
  }

  void validate() {
    Map<String, List<String>> errors = new HashMap<>();
    if (port < 0) {
      errors.put("port", asList("must be a positive number"));
    }
    if (username != null && username.indexOf("@") == -1) {
      errors.putIfAbsent("username", asList("must be a valid email; e.g. foo@meshkorea.net"));
    }

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException(errors.toString());
    }
  }
}
