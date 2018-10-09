规范Dubbo接口定义

关于Dubbo target.jar执行说明,
1、若dependencies jar不放在最终执行target.jar中,则需要将所有dependencies拷贝到一个引导目录中。
<!-- 将依赖的包拷贝到一个目录 -->
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-dependency-plugin</artifactId>
	<executions>
		<execution>
			<id>copy-dependencies</id>
			<phase>package</phase>
			<goals>
				<goal>copy-dependencies</goal>
			</goals>
			<configuration>
				<type>jar</type>
				<includeTypes>jar</includeTypes>
				<overWriteReleases>false</overWriteReleases>
				<overWriteSnapshots>false</overWriteSnapshots>
				<overWriteIfNewer>true</overWriteIfNewer>
				<outputDirectory>
					${project.build.directory}/dependencies_libs
				</outputDirectory>
			</configuration>
		</execution>
	</executions>
</plugin>

此时执行采用命令:
下面的方法废弃,-dirs启动在ExtClassLoader中,dubbo内部没有考虑此问题(此坑很深)
#java -Djava.ext.dirs=dependencies_libs -cp target.jar com.alibaba.dubbo.container.Main
java -cp dependencies_libs/*:target.jar com.alibaba.dubbo.container.Main

2、若dependencies jar放在最终执行target.jar中,需要配置pom的build
<!-- 将依赖的包拷贝到一个jar中 -->
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-dependency-plugin</artifactId>
	<executions>
		<execution>
			<id>copy-dependencies</id>
			<!-- 注意放在package前面 -->
			<phase>prepare-package</phase>
			<goals>
				<goal>copy-dependencies</goal>
			</goals>
			<configuration>
			    <!-- 注意目录位置也不要随意改,放在classes里面 -->
				<outputDirectory>${project.build.directory}/classes/lib</outputDirectory>
				<overWriteReleases>false</overWriteReleases>
				<overWriteSnapshots>false</overWriteSnapshots>
				<overWriteIfNewer>true</overWriteIfNewer>
			</configuration>
		</execution>
	</executions>
</plugin>
此种方法没有测试成功过,jar引用到一起,无法执行sub jar中的类(坑)