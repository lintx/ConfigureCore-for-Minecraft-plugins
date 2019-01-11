package org.lintx.plugins.modules.configure;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Configure {
    protected FileConfiguration config;
    protected String filepath;
    protected JavaPlugin plugin;

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface yamlConfig{
        String path() default "";
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface yamlFile{
        String path() default "config.yml";
    }

    protected Configure(){ }

    public void load(JavaPlugin plugin){
        load(plugin,null);
    }

    public void load(JavaPlugin plugin,String ymlPath){
        this.plugin = plugin;
        loadconfigure(ymlPath);
    }

    private void loadconfigure(String ymlPath){
        File file;
        if (ymlPath!=null){
            file = new File(plugin.getDataFolder(),ymlPath);
            if (!file.exists()) {
                try {
                    InputStream stream = plugin.getResource(ymlPath);
                    if (stream==null){
                        ymlPath = null;
                    }
                }
                catch (Exception e){
                    ymlPath = null;
                }
            }
        }
        if (ymlPath==null){
            yamlFile anno = this.getClass().getAnnotation(yamlFile.class);
            if (anno!=null){
                if (!anno.path().equals("")){
                    ymlPath = anno.path();
                }
            }
        }
        if (ymlPath!=null){
            file = new File(plugin.getDataFolder(),ymlPath);
            if (!file.exists()) {
                try {
                    plugin.saveResource(ymlPath, false);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            this.config = YamlConfiguration.loadConfiguration(file);
            filepath = ymlPath;
            deserialize(config);
        }
    }

    private void deserialize(ConfigurationSection config){
        try {
            if (config==null){
                throw new RuntimeException("config error");
            }
        }
        catch (RuntimeException e){
            e.printStackTrace();
            return;
        }
        Class clz = this.getClass();
        for (Field field : clz.getDeclaredFields()){
            String path = pathWithField(field);
            if (path==null || path.equals("")){
                continue;
            }
            try {
                Object value = config.get(path);
                Class fclz = field.getType();
                if (Map.class.isAssignableFrom(fclz)){
                    if (!(value instanceof ConfigurationSection)){
                        throw new RuntimeException("Class:" + clz + ",Field:" + field.getName() + "'s type is Map, but the config is not map:" + value.toString());
                    }
                    ConfigurationSection section = (ConfigurationSection) value;
                    Map<String,Object> map = new HashMap<String, Object>();
                    for (String key: section.getKeys(false)){
                        map.put(key,section.get(key));
                    }
                    value = map;
                }
                else if (UUID.class.isAssignableFrom(fclz)){
                    value = UUID.fromString((String)value);
                }
                else if (Configure.class.isAssignableFrom(fclz)){
                    if (!(value instanceof ConfigurationSection)){
                        throw new RuntimeException("Class:" + clz + ",Field:" + field.getName() + "'s type is Map, but the config is not map:" + value.toString());
                    }
                    ConfigurationSection section = (ConfigurationSection) value;

                    Configure obj = (Configure) fclz.newInstance();
                    obj.deserialize(section);
                    value = obj;
                }
                else if (List.class.isAssignableFrom(fclz)){
                    value = config.getList(path);
                }
                else if (Enum.class.isAssignableFrom(fclz)){
                    try {
                        value = Enum.valueOf((Class<? extends Enum>)fclz,(String) value);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        continue;
                    }
                }
                if (value!=null){
                    field.set(this,value);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void save(){
        serialize(config);
        File file = new File(plugin.getDataFolder(),filepath);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void serialize(ConfigurationSection config){
        try {
            if (config==null){
                throw new RuntimeException("config error");
            }
        }
        catch (RuntimeException e){
            e.printStackTrace();
            return;
        }
        Class clz = this.getClass();
        for (Field field : clz.getDeclaredFields()){
            String path = pathWithField(field);
            if (path==null){
                continue;
            }
            try {
                Class fclz = field.getType();
                if (Map.class.isAssignableFrom(fclz)){
                    Map map = (Map) field.get(this);
                    if (map==null) continue;
                    ConfigurationSection section = config.createSection(path);

                    for (Object key: map.keySet()){
                        if (!(key instanceof String)){
                            throw new RuntimeException("Class:" + clz + ",Field:" + field.getName() + "'s type is Map, Map's key must string");
                        }
                        section.set((String)key,map.get(key));
                    }
                }
                else if (UUID.class.isAssignableFrom(fclz)){
                    config.set(path,((UUID)field.get(this)).toString());
                }
                else if (Configure.class.isAssignableFrom(fclz)){
                    ConfigurationSection section = config.createSection(path);
                    ((Configure)field.get(this)).serialize(section);
                }
                else if (Enum.class.isAssignableFrom(fclz)){
                    Enum value = (Enum)field.get(this);
                    if (value==null){
                        continue;
                    }
                    config.set(path,value.name());
                }
                else{
                    config.set(path,field.get(this));
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private String pathWithField(Field field){
        String path = null;
        try {
            if (!field.isAnnotationPresent(yamlConfig.class)){
                return null;
            }
            yamlConfig anno = field.getAnnotation(yamlConfig.class);
            if (anno==null){
                return null;
            }
            field.setAccessible(true);
            if (anno.path().equals("")){
                path = field.getName();
            }
            else {
                path = anno.path();
            }
        }
        catch (Exception e){

        }
        return path;
    }
}
