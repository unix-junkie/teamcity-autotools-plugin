package jetbrains.buildServer.autotools.server;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.browser.Element;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AutotoolsDiscoveryExtension extends BreadthFirstRunnerDiscoveryExtension{

  @Override
  protected List<DiscoveredObject> discoverRunnersInDirectory(final Element element, final List<Element> list) {
    return null;
  }
}