package jetbrains.buildServer.autotools.server;

import jetbrains.buildServer.autotools.common.AutotoolsBuildConstants;
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.browser.Element;

import java.util.ArrayList;
import java.util.List;

public class AutotoolsDiscoveryExtension extends BreadthFirstRunnerDiscoveryExtension{

  static final String CONFIGURESCR_NAME = "configure";
  @Override
  protected List<DiscoveredObject> discoverRunnersInDirectory(final Element element, final List<Element> list) {
    ArrayList<DiscoveredObject> discovered = new ArrayList<>();

    for (Element file: list){
      if ((CONFIGURESCR_NAME + ".ac").equalsIgnoreCase(file.getName())){
         discovered.add(new DiscoveredObject(AutotoolsBuildConstants.TYPE, CollectionsUtil.asMap(AutotoolsBuildConstants.UI_SOURCE_PATH, element.getFullName())));
      }
      if ((CONFIGURESCR_NAME + ".in").equalsIgnoreCase(file.getName())){
        discovered.add(new DiscoveredObject(AutotoolsBuildConstants.TYPE, CollectionsUtil.asMap(AutotoolsBuildConstants.UI_SOURCE_PATH, element.getFullName())));
      }
      if ((CONFIGURESCR_NAME).equalsIgnoreCase(file.getName())){
        discovered.add(new DiscoveredObject(AutotoolsBuildConstants.TYPE, CollectionsUtil.asMap(AutotoolsBuildConstants.UI_SOURCE_PATH, element.getFullName())));
      }
    }
    return  discovered;
  }


}