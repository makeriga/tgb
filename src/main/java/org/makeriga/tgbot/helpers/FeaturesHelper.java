package org.makeriga.tgbot.helpers;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.makeriga.tgbot.MakeRigaTgBot;
import org.makeriga.tgbot.Settings;
import org.makeriga.tgbot.features.Feature;

public class FeaturesHelper {
    private static final Logger logger = Logger.getLogger(FeaturesHelper.class);
    
    public static void LoadFeatures(MakeRigaTgBot bot, Settings settings, Map<String, Feature> features) throws Throwable {
        List<Class> allClasses = FeaturesHelper.loadClasses("org.makeriga.tgbot");
        for (Class c : allClasses) {
            if (!c.getSuperclass().equals(Feature.class))
                continue;
            Constructor constructor = c.getConstructor(MakeRigaTgBot.class, Settings.class);
            try {
                Feature f = (Feature)constructor.newInstance(bot, settings);
                assert !features.containsKey(f.GetId());
                features.put(f.GetId(), f);
            }
            catch (Throwable t) {
                // log error
                logger.error("Failed to construct a feature", t);
            }
        }
        
        // init features
        for (Feature f : features.values())
            f.Init();
    }

    public static List<Class> loadClasses(String packageName) throws Throwable {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace(".", "/");
        Enumeration<URL> resources = cl.getResources(path);
        
        List<File> directories = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            directories.add(new File(resource.getFile()));
        }
        
        List<Class> classes = new ArrayList<>();
        for (File d : directories)
            processClasses(d, packageName, classes);
        
        return classes;
    }

    private static List<Class> processClasses(File directory, String packageName, List<Class> classes) throws Throwable {
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory())
                processClasses(file, packageName + "." + file.getName(), classes);
            else if (file.getName().endsWith(".class"))
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
        }
        return classes;
    }
}
