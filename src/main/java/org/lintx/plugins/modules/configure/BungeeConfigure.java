package org.lintx.plugins.modules.configure;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BungeeConfigure {
    private Configuration config;
    private String filepath;
    private Plugin plugin;

    protected BungeeConfigure(){ }

    public void load(Plugin plugin){
        load(plugin,null);
    }

    public void load(Plugin plugin,String ymlPath){
        this.plugin = plugin;
        loadconfigure(ymlPath);
    }

    private void loadconfigure(String ymlPath){
        File file;
        if (ymlPath!=null){
            file = new File(plugin.getDataFolder(),ymlPath);
            if (!file.exists()) {
                try {
                    InputStream stream = plugin.getResourceAsStream(ymlPath);
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
            ConfigureAnnotation.yamlFile anno = this.getClass().getAnnotation(ConfigureAnnotation.yamlFile.class);
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
                    saveResource(ymlPath, false);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            try {
                this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            }
            catch (Exception e){
                this.config = new Configuration();
                e.printStackTrace();
            }
            filepath = ymlPath;
            deserialize(config);
        }
    }

    private void saveResource(String resourcePath, boolean replace){
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = plugin.getResourceAsStream(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in this Plugin");
            } else {
                File outFile = new File(plugin.getDataFolder(), resourcePath);
                int lastIndex = resourcePath.lastIndexOf(47);
                File outDir = new File(plugin.getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }

                try {
                    if (outFile.exists() && !replace) {
                        throw new IllegalArgumentException("Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
                    } else {
                        OutputStream out = new FileOutputStream(outFile);
                        byte[] buf = new byte[1024];

                        int len;
                        while((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        out.close();
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }

    private void deserialize(Configuration config){
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
                    if (!(value instanceof Configuration)){
                        throw new RuntimeException("Class:" + clz + ",Field:" + field.getName() + "'s type is Map, but the config is not map:" + value.toString());
                    }
                    Configuration section = (Configuration) value;
                    Map<String,Object> map = new HashMap<String, Object>();
                    for (String key: section.getKeys()){
                        map.put(key,section.get(key));
                    }
                    value = map;
                }
                else if (UUID.class.isAssignableFrom(fclz)){
                    value = UUID.fromString((String)value);
                }
                else if (BungeeConfigure.class.isAssignableFrom(fclz)){
                    if (!(value instanceof Configuration)){
                        throw new RuntimeException("Class:" + clz + ",Field:" + field.getName() + "'s type is Map, but the config is not map:" + value.toString());
                    }
                    Configuration section = (Configuration) value;

                    BungeeConfigure obj = (BungeeConfigure) fclz.newInstance();
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
            autoCreateFile(file);
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config,file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void autoCreateFile(File file){
        if(file.exists()) {
            return;
        }
        //判断目标文件所在的目录是否存在
        if(!file.getParentFile().exists()) {
            //如果目标文件所在的目录不存在，则创建父目录
            if(!file.getParentFile().mkdirs()) {
                return;
            }
        }
        //创建目标文件
        try {
            boolean result = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void serialize(Configuration config){
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
                    Configuration section = new Configuration();

                    for (Object key: map.keySet()){
                        if (!(key instanceof String)){
                            throw new RuntimeException("Class:" + clz + ",Field:" + field.getName() + "'s type is Map, Map's key must string");
                        }
                        section.set((String)key,map.get(key));
                    }
                    config.set(path,section);
                }
                else if (UUID.class.isAssignableFrom(fclz)){
                    config.set(path,((UUID)field.get(this)).toString());
                }
                else if (BungeeConfigure.class.isAssignableFrom(fclz)){
                    Configuration section = new Configuration();
                    ((BungeeConfigure)field.get(this)).serialize(section);
                    config.set(path,section);
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
            if (!field.isAnnotationPresent(ConfigureAnnotation.yamlConfig.class)){
                return null;
            }
            ConfigureAnnotation.yamlConfig anno = field.getAnnotation(ConfigureAnnotation.yamlConfig.class);
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
