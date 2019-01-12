package org.lintx.plugins.modules.configure;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BukkitConfigure extends Configure {
    private FileConfiguration config;
    private String filepath;
    private JavaPlugin plugin;

    protected BukkitConfigure(){ }

    public void load(JavaPlugin plugin){
        load(plugin,this,null);
    }
    public void load(JavaPlugin plugin,String ymlPath){
        load(plugin,this,ymlPath);
    }

    public void load(JavaPlugin plugin,Object object){
        load(plugin,object,null);
    }

    public void load(JavaPlugin plugin,Object object,String ymlPath){
        this.plugin = plugin;
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
            ymlPath = pathWithClass(object.getClass());
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
            deserialize(config,object);
        }
    }

    public void save(){
        save(plugin,this,config,filepath);
    }

    public void save(JavaPlugin plugin,Object object){
        save(plugin,object,null);
    }

    public void save(JavaPlugin plugin,Object object,String ymlPath){
        save(plugin,object,new YamlConfiguration(),ymlPath);
    }

    private void save(JavaPlugin plugin,Object object,FileConfiguration config,String ymlPath){
        File file;
        if (ymlPath==null){
            ymlPath = pathWithClass(object.getClass());
        }
        if (ymlPath!=null){
            file = new File(plugin.getDataFolder(),ymlPath);
            serialize(config,object);
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deserialize(ConfigurationSection config,Object object){
        try {
            if (config==null){
                throw new RuntimeException("config error");
            }
        }
        catch (RuntimeException e){
            e.printStackTrace();
            return;
        }
        Class clz = object.getClass();
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
                else if (clzHasAnnotation(fclz)){
                    if (!(value instanceof ConfigurationSection)){
                        throw new RuntimeException("Class:" + clz + ",Field:" + field.getName() + "'s type is Map, but the config is not map:" + value.toString());
                    }
                    ConfigurationSection section = (ConfigurationSection) value;

                    Object obj = fclz.newInstance();
                    deserialize(section,obj);
                    value = obj;
                }
                if (value!=null){
                    field.set(object,value);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void serialize(ConfigurationSection config,Object object){
        try {
            if (config==null){
                throw new RuntimeException("config error");
            }
        }
        catch (RuntimeException e){
            e.printStackTrace();
            return;
        }
        Class clz = object.getClass();
        for (Field field : clz.getDeclaredFields()){
            String path = pathWithField(field);
            if (path==null){
                continue;
            }
            try {
                Class fclz = field.getType();
                if (Map.class.isAssignableFrom(fclz)){
                    Map map = (Map) field.get(object);
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
                    config.set(path,((UUID)field.get(object)).toString());
                }
                else if (Enum.class.isAssignableFrom(fclz)){
                    Enum value = (Enum)field.get(object);
                    if (value==null){
                        continue;
                    }
                    config.set(path,value.name());
                }
                else if (clzHasAnnotation(fclz)){
                    ConfigurationSection section = config.createSection(path);
                    serialize(section,field.get(object));
                }
                else{
                    config.set(path,field.get(object));
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
