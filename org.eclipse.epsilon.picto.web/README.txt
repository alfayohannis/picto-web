mvn dependency:purge-local-repository -DmanualInclude="org.eclipse.ui:org.eclipse.ui.workbench"

mvn install:install-file -Dfile=C:\eclipse.vaultage\plugins\org.eclipse.swt.win32.win32.x86_64_3.117.0.v20210906-0842.jar -DgroupId=org.eclipse -DartifactId=org.eclipse.swt.win32 -Dversion=3.117.0 -Dpackaging=jar

mvn install:install-file -Dfile=C:\eclipse.vaultage\plugins\org.eclipse.epsilon.common.dt_2.5.0.202203261147.jar -DgroupId=org.eclipse.epsilon -DartifactId=org.eclipse.epsilon.common.dt -Dversion=2.5.0 -Dpackaging=jar

mvn install:install-file -Dfile=C:\eclipse.vaultage\plugins\org.eclipse.epsilon.eol.dt_2.5.0.202203261147.jar -DgroupId=org.eclipse.epsilon -DartifactId=org.eclipse.epsilon.eol.dt -Dversion=2.5.0 -Dpackaging=jar

mvn install:install-file -Dfile=C:\eclipse.vaultage\plugins\org.eclipse.epsilon.emf.dt_2.5.0.202203261147.jar -DgroupId=org.eclipse.epsilon -DartifactId=org.eclipse.epsilon.emf.dt -Dversion=2.5.0 -Dpackaging=jar

mvn install:install-file -Dfile=C:\eclipse.vaultage\plugins\org.eclipse.epsilon.picto_2.5.0.202203261147.jar -DgroupId=org.eclipse.epsilon -DartifactId=org.eclipse.epsilon.picto -Dversion=2.5.0 -Dpackaging=jar

mvn install:install-file -Dfile=C:\eclipse.vaultage\plugins\org.eclipse.epsilon.picto_2.5.0.202203261147.jar -DgroupId=org.eclipse.epsilon -DartifactId=org.eclipse.epsilon.picto -Dversion=2.5.0 -Dpackaging=jar

mvn install:install-file -Dfile=C:\eclipse.vaultage\plugins\org.eclipse.core.resources_3.15.100.v20210818-1523.jar -DgroupId=org.eclipse.core -DartifactId=org.eclipse.core.resources -Dversion=3.15.100 -Dpackaging=jar

mvn install:install-file -Dfile=C:\eclipse.vaultage\plugins\org.eclipse.ui.workbench_3.123.0.v20210817-0704.jar -DgroupId=org.eclipse.ui -DartifactId=org.eclipse.ui.workbench -Dversion=3.123.0 -Dpackaging=jar
