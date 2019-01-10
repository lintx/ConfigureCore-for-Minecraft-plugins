### ConfigureCore
how to use:
1. download the release jar to your project.
2. create the class extend the `org.lintx.plugins.module.configure.Configure`.
3. use the Annotation to serialize your config.
if your use the module, your plugins must rely on this.

don't add the source file to your project.

如何使用:
1. 下载编译包放到你的工程目录并添加到库.
2. 创建配置类并继承`org.lintx.plugins.module.configure.Configure`.
3. 使用注解解析配置文件.
4. 如果你使用了这个库, 你的插件必须依赖这个插件.
不要把源代码直接放进你的工程目录, 以免造成冲突, 除非你修改包名.

[Examples](https://github.com/lintx/bukkitapi-configure-example)