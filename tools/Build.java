import javax.tools.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;
/** Offline build: java tools/Build.java [--test]. */
public class Build {
    static final Path ROOT=Path.of("").toAbsolutePath();
    static void clean(Path p)throws IOException {
        if(!p.normalize().startsWith(ROOT.resolve("out")))throw new IOException("Unsafe output path");
        if(Files.exists(p))try(Stream<Path> walk=Files.walk(p)) {
            for(Path f:walk.sorted(Comparator.reverseOrder()).toList())Files.delete(f);
        }
        Files.createDirectories(p);
    }
    static List<Path> javaFiles(Path p)throws IOException {
        try(Stream<Path> walk=Files.walk(p)) {
            return walk.filter(f->f.toString().endsWith(".java")).sorted().toList();
        }
    }
    static void compile(JavaCompiler compiler,List<Path> sources,Path output,String cp)throws Exception {
        List<String> flags=new ArrayList<>(List.of("--release","17","-encoding","UTF-8","-Xlint:all,-serial","-d",output.toString()));
        if(cp!=null) {
            flags.add("-classpath");
            flags.add(cp);
        }
        try(StandardJavaFileManager fm=compiler.getStandardFileManager(null,Locale.ROOT,java.nio.charset.StandardCharsets.UTF_8)) {
            if(!compiler.getTask(null,fm,null,flags,null,fm.getJavaFileObjectsFromPaths(sources)).call())throw new IOException("Compile gagal");
        }
    }
    static void run(String...args)throws Exception {
        String exe=Path.of(System.getProperty("java.home"),"bin",System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win")?"java.exe":"java").toString();
        List<String> command=new ArrayList<>();
        command.add(exe);
        command.addAll(List.of(args));
        int code=new ProcessBuilder(command).inheritIO().start().waitFor();
        if(code!=0)throw new IOException("Proses keluar dengan kode "+code);
    }
    public static void main(String[] args)throws Exception {
        if(!Files.isRegularFile(ROOT.resolve("pom.xml")))throw new IOException("Jalankan dari folder ADayInMyCity");
        JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
        if(compiler==null)throw new IOException("Gunakan JDK 17+, bukan JRE.");
        Path classes=ROOT.resolve("out/classes"),builder=ROOT.resolve("out/builder"),jar=ROOT.resolve("dist/ADayInMyCity.jar");
        clean(classes);
        clean(builder);
        Files.createDirectories(jar.getParent());
        compile(compiler,javaFiles(ROOT.resolve("src/main/java")),classes,null);
        Path resources=ROOT.resolve("src/main/resources");
        try(Stream<Path> walk=Files.walk(resources)) {
            for(Path p:walk.toList()) {
                Path to=classes.resolve(resources.relativize(p));
                if(Files.isDirectory(p))Files.createDirectories(to);
                else Files.copy(p,to,StandardCopyOption.REPLACE_EXISTING);
            }
        }
        compile(compiler,List.of(ROOT.resolve("tools/JarBuilder.java")),builder,null);
        run("-cp",builder.toString(),"JarBuilder",classes.toString(),jar.toString(),"com.innoelektrica.polymorphism.Main");
        run("-Djava.awt.headless=true","-jar",jar.toString(),"--check-resources");
        if(List.of(args).contains("--test")) {
            Path tests=ROOT.resolve("out/test-classes");
            clean(tests);
            compile(compiler,javaFiles(ROOT.resolve("src/test/java")),tests,jar.toString());
            run("-Djava.awt.headless=true","-cp",tests+File.pathSeparator+jar,"com.innoelektrica.polymorphism.game.GameTests");
        }
        System.out.println("BUILD OK: "+jar+" (Java 17)");
    }
}
