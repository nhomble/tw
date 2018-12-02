package org.hombro.jhu.tw.exceptions;

public class MissingUserException extends RuntimeException {

  public MissingUserException(String user) {
    super("Failed to find entry for twitchUser=" + user);
  }
}
