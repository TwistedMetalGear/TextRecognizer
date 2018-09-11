// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.willowtree.textrecognizer.textrecognition;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionText.Element;
import com.google.firebase.ml.vision.text.FirebaseVisionText.Line;
import com.google.firebase.ml.vision.text.FirebaseVisionText.TextBlock;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.willowtree.textrecognizer.FrameMetadata;
import com.willowtree.textrecognizer.GraphicOverlay;
import com.willowtree.textrecognizer.VisionProcessorBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Processor for the text recognition demo.
 */
public class TextRecognitionProcessor extends VisionProcessorBase<FirebaseVisionText> {

    private static final String TAG = "TextRecProc";

    private final FirebaseVisionTextRecognizer detector;
    private final Context context;
    private String query = "";

    public TextRecognitionProcessor(Context context) {
        this.context = context;
        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
        }
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }

    @Override
    protected void onSuccess(
            @NonNull FirebaseVisionText results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();

        List<TextBlock> blocks = results.getTextBlocks();

        String[] splitText = query.split(" ");
        List<Rect> rects = new ArrayList<>();

        for (int i = 0; i < blocks.size(); i++) {
            List<Line> lines = blocks.get(i).getLines();

            Element firstMatchingElement = null;
            Element lastMatchingElement = null;

            int index = 0;

            for (int j = 0; j < lines.size(); j++) {
                List<Element> elements = lines.get(j).getElements();

                // Loop through the words in the line looking for a match with the
                // current word in the query.
                for (int k = 0; k < elements.size(); k++) {
                    Element element = elements.get(k);

                    if (element.getText().equalsIgnoreCase(splitText[index])) {
                        if (index == 0) {
                            firstMatchingElement = element;
                        }

                        if (index == splitText.length - 1) {
                            lastMatchingElement = element;
                        }

                        index++;

                        // This condition takes care of a couple of possibilities:
                        // 1) This is the last word of the line and we haven't fully matched the
                        //    query. We will create a potential rectangle to draw and continue
                        //    searching for matches on the next line.
                        // 2) We fully matched the query. Create a rectangle to draw.
                        if (k == elements.size() - 1 || lastMatchingElement != null) {
                            if (rects.isEmpty()) {
                                rects.add(createRect(firstMatchingElement, element));
                            } else {
                                rects.add(createRect(elements.get(0), element));
                            }
                        }

                        if (lastMatchingElement != null) {
                            // Query has been fully matched, draw rectangle(s).
                            for (Rect rect : rects) {
                                GraphicOverlay.Graphic textGraphic = new TextGraphic(context, graphicOverlay, rect);
                                graphicOverlay.add(textGraphic);
                            }

                            // Reset data and continue searching for occurrences of the query.
                            index = 0;
                            firstMatchingElement = null;
                            lastMatchingElement = null;
                            rects.clear();
                        }
                    } else {
                        // Found a word that doesn't match. Reset data and continue searching
                        // for occurrences of the query.
                        index = 0;
                        firstMatchingElement = null;
                        lastMatchingElement = null;
                        rects.clear();
                    }
                }
            }
        }
    }


    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Text detection failed." + e);
    }

    /**
     * Creates a rectangle encompassing the first and last words (and hence, everything in between)
     * of a string of text.
     */
    private Rect createRect(Element first, Element last) {
        Rect firstRect = first.getBoundingBox();
        Rect lastRect = last.getBoundingBox();

        Rect rect = new Rect();
        rect.left = firstRect.left;
        rect.right = lastRect.right;

        if (firstRect.top < lastRect.top) {
            rect.top = firstRect.top;
        } else {
            rect.top = lastRect.top;
        }

        if (firstRect.bottom > lastRect.bottom) {
            rect.bottom = firstRect.bottom;
        } else {
            rect.bottom = lastRect.bottom;
        }

        return rect;
    }
}