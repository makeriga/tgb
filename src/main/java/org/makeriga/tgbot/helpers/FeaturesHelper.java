package org.makeriga.tgbot.helpers;

import org.makeriga.tgbot.MakeRigaTgBot;
import org.makeriga.tgbot.Settings;
import org.makeriga.tgbot.features.Feature;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.reflections.scanners.Scanners.SubTypes;

public class FeaturesHelper {
    private static final Logger logger = LoggerFactory.getLogger(FeaturesHelper.class);
    
    public static void LoadFeatures(MakeRigaTgBot bot, Settings settings, Map<String, Feature> features) throws Throwable {
        Reflections reflections = new Reflections("org.makeriga.tgbot");
        Set<Class<?>> allClasses = reflections.get(SubTypes.of(Feature.class).asClass());
        if(allClasses.isEmpty()){
            logger.warn("Error loading features");
        }
        for (Class<?> c : allClasses) {
            if (!c.getSuperclass().equals(Feature.class))
                continue;
            Constructor constructor = c.getConstructor(MakeRigaTgBot.class, Settings.class);
            try {
                Feature f = (Feature) constructor.newInstance(bot, settings);
                if (features.containsKey(f.GetId())) throw new AssertionError();
                if (f.GetId().contains("|")) throw new AssertionError();
                features.put(f.GetId(), f);
            } catch (Throwable t) {
                // log error
                logger.error("Failed to construct a feature", t);
            }
        }

        // init features
        for (Feature f : features.values())
            f.Init();
    }
}
