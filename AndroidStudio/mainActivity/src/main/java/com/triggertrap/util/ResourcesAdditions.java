package com.triggertrap.util;

/**
 * Created by scottmellors on 03/09/2014.
 */

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author Cyril Mottier
 */
public final class ResourcesAdditions {

    private ResourcesAdditions() {
        // No instance
    }

    public static Bundle getResourcesExtras(Resources res, String rootTag,
                                            int resId) throws Resources.NotFoundException {
        XmlResourceParser parser = res.getXml(resId);

        int type;
        try {
            while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
                // Empty loop
            }
            if (type != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("No start tag found");
            }
            if (!parser.getName().equals(rootTag)) {
                throw new XmlPullParserException("Unknown start tag. Should be '" + rootTag + "'");
            }

            final Bundle extras = new Bundle();
            res.parseBundleExtras(parser, extras);

            return extras;

        } catch (Exception e) {
            final Resources.NotFoundException nfe = new Resources.NotFoundException();
            nfe.initCause(e);
            throw nfe;
        }
    }

}
