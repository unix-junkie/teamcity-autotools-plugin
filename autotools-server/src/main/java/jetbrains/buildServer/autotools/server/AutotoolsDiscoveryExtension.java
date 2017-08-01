package jetbrains.buildServer.autotools.server;

import jetbrains.buildServer.autotools.common.AutotoolsBuildConstants;
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.browser.Element;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Created on 12.07.2017.
 * @author     : Nadezhda Demina
 */
public final class AutotoolsDiscoveryExtension extends BreadthFirstRunnerDiscoveryExtension{

  /**
   * Field with name of file to should be discovered.
   */
  private static final String CONFIGURESCR_NAME = "configure";

  @Override
  protected List<DiscoveredObject> discoverRunnersInDirectory(final @NotNull Element element, final @NotNull List<Element>  filesList) {
    final List<DiscoveredObject> discovered = new ArrayList<>();

    for (final Element file: filesList){

      if (file.getName().equalsIgnoreCase(CONFIGURESCR_NAME + ".ac") || file.getName().equalsIgnoreCase(CONFIGURESCR_NAME + ".in")
        || file.getName().equalsIgnoreCase(CONFIGURESCR_NAME)){
        discovered.add(new DiscoveredObject(AutotoolsBuildConstants.TYPE, CollectionsUtil.asMap(AutotoolsBuildConstants.UI_SOURCE_PATH, element.getFullName())));
      }
    }
    return  discovered;
  }
}