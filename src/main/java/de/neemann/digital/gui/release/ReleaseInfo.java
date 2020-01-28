/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.release;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.prefs.Preferences;

/**
 * Checks for new releases
 */
public final class ReleaseInfo {
    private static final String RELEASE_URL = "https://api.github.com/repos/hneemann/Digital/releases/latest";
    private static final String RELEASE_URL_NEW = "https://api.github.com/repos/hneemann/DigitaSi/releases/latest";

    private String version;
    private String url;

    /**
     * Creates a new instance
     *
     * @throws IOException IOException
     */
    ReleaseInfo() throws IOException {
        try {
            readReleaseInfo(RELEASE_URL_NEW);
            Preferences.userRoot().node("dig").put("newname", "DigitaSi");
        } catch (IOException e) {
            readReleaseInfo(RELEASE_URL);
        }
    }

    private void readReleaseInfo(String releaseUrl) throws IOException {
        try (InputStream in = new URL(releaseUrl).openStream()) {
            JSONTokener tok = new JSONTokener(in);
            JSONObject obj = new JSONObject(tok);

            version = obj.get("tag_name").toString();
            url = obj.get("html_url").toString();
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * @return the actual version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the url of the release page
     */
    public String getUrl() {
        return url;
    }

}
