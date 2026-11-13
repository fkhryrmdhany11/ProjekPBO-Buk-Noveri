package com.innoelektrica.polymorphism.util;
import javax.sound.sampled.*;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
/** Dual-channel audio player: satu kanal SFX (opsional batas durasi) & satu kanal loop mesin kendaraan. */
public final class AudioPlayer {
    private static final ExecutorService WORKER=Executors.newSingleThreadExecutor(r-> {
        Thread t=new Thread(r,"city-audio");
        t.setDaemon(true);
        return t;
    });
    private static final ScheduledExecutorService TIMER=Executors.newSingleThreadScheduledExecutor(r-> {
        Thread t=new Thread(r,"city-audio-timer");
        t.setDaemon(true);
        return t;
    });
    private static final AtomicLong GENERATION=new AtomicLong();
    private static final AtomicLong ENGINE_GEN=new AtomicLong();
    private static final Object ENGINE_STATE_LOCK=new Object();
    private static Clip currentClip,engineClip;
    private static ScheduledFuture<?> pendingStopTask;
    private static volatile String requestedEngineResource,activeEngineResource;
    private static volatile Consumer<String> errors=s-> {
    };
    private AudioPlayer() {
    }
    public static void setErrorHandler(Consumer<String> handler) {
        errors=handler;
    }
    private static Clip openClip(String resource) throws Exception {
        byte[] data;
        try(InputStream raw=AudioPlayer.class.getResourceAsStream(resource)) {
            if(raw==null)throw new FileNotFoundException(resource);
            data=raw.readAllBytes();
        }
        try(AudioInputStream stream=AudioSystem.getAudioInputStream(new ByteArrayInputStream(data))) {
            Clip clip=AudioSystem.getClip();
            clip.open(stream);
            return clip;
        }
    }
    private static void cancelPendingStop() {
        if(pendingStopTask!=null) {
            pendingStopTask.cancel(false);
            pendingStopTask=null;
        }
    }
    private static void closeCurrent() {
        cancelPendingStop();
        if(currentClip!=null) {
            Clip c=currentClip;
            currentClip=null;
            try {
                c.stop();
                c.close();
            }
            catch(Exception ignored) {
            }
        }
    }
    private static void closeEngine() {
        activeEngineResource=null;
        if(engineClip!=null) {
            Clip c=engineClip;
            engineClip=null;
            try {
                c.stop();
                c.close();
            }
            catch(Exception ignored) {
            }
        }
    }
    public static void play(String resource) {
        playShort(resource,0);
    }
    /** Memutar suara satu kali, otomatis berhenti bila melebihi maxMillis (> 0) agar suara hewan tidak kepanjangan. */
    public static void playShort(String resource,int maxMillis) {
        if(resource==null||GraphicsEnvironment.isHeadless())return;
        long token=GENERATION.incrementAndGet();
        WORKER.execute(()-> {
            closeCurrent();
            Clip clip=null;
            try {
                clip=openClip(resource);
                if(token!=GENERATION.get()) {
                    clip.close();
                    return;
                }
                Clip selected=clip;
                currentClip=selected;
                selected.addLineListener(e-> {
                    if(e.getType()==LineEvent.Type.STOP)WORKER.execute(()-> {
                        if(currentClip==selected) {
                            cancelPendingStop();
                            currentClip=null;
                        }
                        try {
                            selected.close();
                        }
                        catch(Exception ignored) {
                        }
                    });
                });
                selected.start();
                if(maxMillis>0) {
                    pendingStopTask=TIMER.schedule(()->WORKER.execute(()-> {
                        if(token==GENERATION.get()&&currentClip==selected) {
                            closeCurrent();
                        }
                    }),maxMillis,TimeUnit.MILLISECONDS);
                }
            }
            catch(Exception ex) {
                if(clip!=null)clip.close();
                if(token==GENERATION.get())SwingUtilities.invokeLater(()->errors.accept("Suara belum dapat diputar pada perangkat ini. Periksa keluaran audio."));
            }
        });
    }
    public static void playEngineLoop(String resource) {
        if(resource==null||GraphicsEnvironment.isHeadless())return;
        long token;
        synchronized(ENGINE_STATE_LOCK) {
            if(resource.equals(requestedEngineResource)) {
                return;
            }
            requestedEngineResource=resource;
            token=ENGINE_GEN.incrementAndGet();
        }
        WORKER.execute(()-> {
            if(token!=ENGINE_GEN.get()||!resource.equals(requestedEngineResource))return;
            if(resource.equals(activeEngineResource)&&engineClip!=null&&engineClip.isRunning())return;
            closeEngine();
            Clip clip=null;
            try {
                clip=openClip(resource);
                if(token!=ENGINE_GEN.get()||!resource.equals(requestedEngineResource)) {
                    clip.close();
                    return;
                }
                if(clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gain=(FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
                    gain.setValue(Math.max(gain.getMinimum(),Math.min(gain.getMaximum(),-3.5f)));
                }
                engineClip=clip;
                activeEngineResource=resource;
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            catch(Exception ex) {
                if(clip!=null)clip.close();
                activeEngineResource=null;
                synchronized(ENGINE_STATE_LOCK) {
                    if(token==ENGINE_GEN.get())requestedEngineResource=null;
                }
            }
        });
    }
    public static void stopEngineLoop() {
        long token;
        synchronized(ENGINE_STATE_LOCK) {
            if(requestedEngineResource==null&&activeEngineResource==null)return;
            requestedEngineResource=null;
            token=ENGINE_GEN.incrementAndGet();
        }
        if(GraphicsEnvironment.isHeadless()) {
            activeEngineResource=null;
            return;
        }
        WORKER.execute(()-> {
            if(token==ENGINE_GEN.get()&&requestedEngineResource==null) {
                closeEngine();
            }
        });
    }
    public static void stopAll() {
        GENERATION.incrementAndGet();
        synchronized(ENGINE_STATE_LOCK) {
            requestedEngineResource=null;
            ENGINE_GEN.incrementAndGet();
        }
        if(GraphicsEnvironment.isHeadless()) {
            activeEngineResource=null;
            return;
        }
        WORKER.execute(()-> {
            closeCurrent();
            closeEngine();
        });
    }
}
