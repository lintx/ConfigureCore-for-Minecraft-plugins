package org.lintx.plugins.modules.configure;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Configure {
    protected FileConfiguration config;
    protected String filepath;
    protected JavaPlugin plugin;

    @Retention(RetentionPolicy.RUNTIME)
    public @interface yamlConfig{
        String path() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface yamlFile{
        String path() default "config.yml";
    }

    protected Configure(){ }

    public void load(JavaPlugin plugin){
        this.plugin = plugin;
        loadconfigure();
    }

    private void loadconfigure(){
        Class clz = this.getClass();
        FileConfiguration config = null;
        yamlFile anno = this.getClass().getAnnotation(yamlFile.class);
        if (anno!=null){
            if (!anno.path().equals("")){
                File file = new File(plugin.getDataFolder(),anno.path());
                if (!file.exists()) {
                    plugin.saveResource(anno.path(), false);
                }
                config = YamlConfiguration.loadConfiguration(file);
                filepath = anno.path();
            }
        }
        this.config = config;
        deserialize(config);
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
                    //Type ftype = field.getGenericType();
//                    if (!(ftype instanceof ParameterizedType)){
//                        throw new RuntimeException("Class:" + clz + ",Field:" + field.getName() + "'s type is List, but the GenericType cannot read");
//                    }
//                    ParameterizedType fptype = (ParameterizedType) ftype;
//                    Type[] types = fptype.getActualTypeArguments();
//                    if (types.length!=1){
//                        throw new RuntimeException("Class:" + clz + ",Field:" + field.getName() + "'s type is List, but the GenericType's count not 1");
//                    }
                    //Class typeclz = (Class) types[0];
                    value = config.getList(path);
                    //if (String.class.isAssignableFrom(typeclz)){

                    //}
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
