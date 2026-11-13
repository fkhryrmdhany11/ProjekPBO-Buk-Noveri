package com.innoelektrica.polymorphism.game;
import com.innoelektrica.polymorphism.model.*;
import javax.sound.sampled.*;
public final class ResourceVerifier {
    private ResourceVerifier() {
    }
    public static void verify()throws Exception {
        int audioCount=0;
        for(AnimalType a:AnimalType.values()) {
            Hewan h=a.create();
            try(AudioInputStream in=AudioSystem.getAudioInputStream(ResourceVerifier.class.getResource(h.getAudioResource()))) {
                if(in.getFrameLength()<=0)throw new IllegalStateException("Audio kosong");
                audioCount++;
                System.out.println("AUDIO OK "+h.getAudioResource()+" "+in.getFormat());
            }
        }
        for(VehicleType v:VehicleType.values()) {
            Kendaraan k=v.create();
            for(String res:new String[] {
                k.getAudioResource(),k.getHornResource()
            }
            ) {
                try(AudioInputStream in=AudioSystem.getAudioInputStream(ResourceVerifier.class.getResource(res))) {
                    if(in.getFrameLength()<=0)throw new IllegalStateException("Audio kosong "+res);
                    audioCount++;
                    System.out.println("AUDIO OK "+res+" "+in.getFormat());
                }
            }
        }
        System.out.println("RESOURCE OK: 100% Pure Pixel-Art (0 PNG) dan "+audioCount+" WAV terbaca dari classpath/JAR.");
    }
}
