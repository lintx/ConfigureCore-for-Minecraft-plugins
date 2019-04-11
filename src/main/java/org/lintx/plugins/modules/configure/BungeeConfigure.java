package org.lintx.plugins.modules.configure;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class BungeeConfigure extends Configure {
    private Configuration config;
    private String filepath;
    private Plugin plugin;

    protected BungeeConfigure(){ }

    public void load(Plugin plugin){
        load(plugin,this,null);
    }

    public void load(Plugin plugin,String ymlPath){
        load(plugin,this,ymlPath);
    }

    public void load(Plugin plugin,Object object){
        load(plugin,object,null);
    }

    public void load(Plugin plugin,Object object,String ymlPath){
        this.plugin = plugin;
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
            ymlPath = pathWithClass(object.getClass());
        }
        if (ymlPath!=null){
            file = new File(plugin.getDataFolder(),ymlPath);
            if (!file.exists()) {
                try {
                    saveResource(ymlPath, false);
                }
                catch (IllegalArgumentException e){
                    plugin.getLogger().warning(e.getMessage());
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
            deserialize(config,object);
        }
    }

    public void save(){
        save(plugin,this,filepath,config);
    }

    public void save(Plugin plugin,Object object){
        save(plugin,object,null);
    }

    public void save(Plugin plugin,Object object,String ymlPath){
        save(plugin,object,ymlPath,new Configuration());
    }

    private void save(Plugin plugin,Object object,String ymlPath,Configuration config){
        File file;
        if (ymlPath==null){
            ymlPath = pathWithClass(object.getClass());
        }
        if (ymlPath!=null){
            file = new File(plugin.getDataFolder(),ymlPath);
            serialize(config,object);
            try {
                autoCreateFile(file);
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(config,file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deserialize(Configuration config,Object object){
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
                else if (List.class.isAssignableFrom(fclz)){
                    List list = config.getList(path);
                    if (list==null || list.size()==0){
                        continue;
                    }
                    Type genericType = field.getGenericType();
                    if (genericType==null){
                        continue;
                    }
                    if (genericType instanceof ParameterizedType){
                        List<Object> val = new ArrayList<Object>();
                        ParameterizedType pt = (ParameterizedType)genericType;
                        Class<?> genericClz = (Class<?>)pt.getActualTypeArguments()[0];

                        if (clzHasAnnotation(genericClz)){
                            for (Object o : list) {
                                Constructor constructor = config.getClass().getDeclaredConstructor(Map.class,Configuration.class);
                                if (constructor!=null){
                                    constructor.setAccessible(true);
                                    Configuration section = (Configuration)constructor.newInstance((Map)o,null);
                                    Object obj = genericClz.newInstance();
                                    deserialize(section,obj);
                                    val.add(obj);
                                }
                            }
                        }
                        else {
                            val.addAll(list);
                        }
                        field.set(object,val);
                    }
                    continue;
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
                    if (!(value instanceof Configuration)){
                        throw new RuntimeException("Class:" + clz + ",Field:" + field.getName() + "'s type is Custom Object, but the config is not Custom Object:" + value.toString());
                    }
                    Configuration section = (Configuration) value;
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

    private void serialize(Configuration config,Object object){
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
                    config.set(path,((UUID)field.get(object)).toString());
                }
                else if (List.class.isAssignableFrom(fclz)){
                    List list = (List) field.get(object);
                    if (list==null || list.size()==0){
                        continue;
                    }
                    Type genericType = field.getGenericType();
                    if (genericType==null){
                        continue;
                    }
                    if (genericType instanceof ParameterizedType){
                        List<Object> val = new ArrayList<Object>();
                        ParameterizedType pt = (ParameterizedType)genericType;
                        Class<?> genericClz = (Class<?>)pt.getActualTypeArguments()[0];
                        if (clzHasAnnotation(genericClz)){
                            for (Object o : list) {
                                Configuration section = new Configuration();
                                serialize(section,o);
                                val.add(section);
                            }
                        }
                        else {
                            val.addAll(list);
                        }
                        config.set(path,val);
                    }
                }
                else if (Enum.class.isAssignableFrom(fclz)){
                    Enum value = (Enum)field.get(object);
                    if (value==null){
                        continue;
                    }
                    config.set(path,value.name());
                }
                else if (clzHasAnnotation(fclz)){
                    Configuration section = new Configuration();
                    serialize(section,field.get(object));
                    config.set(path,section);
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
}
