import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.stream.*;
public class JarBuilder {
    public static void main(String[] args)throws Exception {
        Path classes=Path.of(args[0]),target=Path.of(args[1]);
        Files.createDirectories(target.toAbsolutePath().getParent());
        Path temp=Files.createTempFile(target.toAbsolutePath().getParent(),"city-jar-",".tmp");
        try {
            Manifest manifest=new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,"1.0");
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,args[2]);
            try(JarOutputStream out=new JarOutputStream(Files.newOutputStream(temp),manifest);
            Stream<Path> walk=Files.walk(classes)) {
                for(Path p:walk.filter(Files::isRegularFile).sorted().toList()) {
                    String name=classes.relativize(p).toString().replace(File.separatorChar,'/');
                    JarEntry entry=new JarEntry(name);
                    entry.setTime(0);
                    out.putNextEntry(entry);
                    Files.copy(p,out);
                    out.closeEntry();
                }
            }
            try(JarFile check=new JarFile(temp.toFile())) {
                if(check.getManifest()==null||check.size()<2)throw new IOException("JAR tidak lengkap");
            }
            try {
                Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
            }
            catch(AtomicMoveNotSupportedException ex) {
                Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally {
            Files.deleteIfExists(temp);
        }
    }
}
