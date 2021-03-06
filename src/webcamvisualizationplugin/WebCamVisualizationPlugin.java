package webcamvisualizationplugin;

import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.organization.Configuration;
import mo.organization.ProjectOrganization;
import mo.organization.StagePlugin;
import mo.visualization.VisualizationProvider;

@Extension(
        xtends = {
            @Extends(
                    extensionPointId = "mo.visualization.VisualizationProvider"
            )
        }
)
public class WebCamVisualizationPlugin implements VisualizationProvider {

    private final static String PLUGIN_NAME = "WebCam Visualization";
    
    List<Configuration> configs;
    private static final Logger logger = Logger.getLogger(WebCamVisualizationPlugin.class.getCanonicalName());

    public WebCamVisualizationPlugin() {
        configs = new ArrayList<>();
    }
    
    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public Configuration initNewConfiguration(ProjectOrganization organization) {
        WCDialog d = new WCDialog(organization);
        
        if (d.showDialog()) {
            WCVisConfig c = new WCVisConfig();
            c.setId(d.getConfigurationName());
            configs.add(c);
            return c;
        }
        
        return null;
    }

    @Override
    public List<Configuration> getConfigurations() {
        return configs;
    }

    @Override
    public StagePlugin fromFile(File file) {
        if (file.isFile()) {
            try {
                WebCamVisualizationPlugin mc = new WebCamVisualizationPlugin();
                XElement root = XIO.readUTF(new FileInputStream(file));
                XElement[] pathsX = root.getElements("path");
                for (XElement pathX : pathsX) {
                    String path = pathX.getString();
                    WCVisConfig c = new WCVisConfig();
                    Configuration config = c.fromFile(new File(file.getParentFile(), path));
                    if (config != null) {
                        mc.configs.add(config);
                    }
                }
                return mc;
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    @Override
    public File toFile(File parent) {
        File file = new File(parent, "webcam-visualization.xml");
        if (!file.isFile()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        XElement root = new XElement("vis");
        for (Configuration config : configs) {
            File p = new File(parent, "webcam-visualization");
            p.mkdirs();
            File f = config.toFile(p);

            XElement path = new XElement("path");
            Path parentPath = parent.toPath();
            Path configPath = f.toPath();
            path.setString(parentPath.relativize(configPath).toString());
            root.addElement(path);
        }
        try {
            XIO.writeUTF(root, new FileOutputStream(file));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return file;
    }
    
}
