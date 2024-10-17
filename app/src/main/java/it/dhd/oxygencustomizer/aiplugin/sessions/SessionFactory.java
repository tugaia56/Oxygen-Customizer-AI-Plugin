package it.dhd.oxygencustomizer.aiplugin.sessions;

import android.content.Context;

import java.io.IOException;

import ai.onnxruntime.OrtException;

public class SessionFactory {

    public static BaseSession createSession(Context context, int sessionId) throws IOException, OrtException {

        return switch (sessionId) {
            case 1 -> new U2netHumanSession(context);
            case 2 -> new U2netPSession(context);
            case 3 -> new IsnetAnimeSession(context);
            default -> new U2netSession(context);
        };
    }

}
