/*
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.example.simanishi.fuufuu.awsS3.util;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* 
 * This class just handles getting the client since we don't need to have more than
 * one per application
 */
public class Util {
    private static AmazonS3Client sS3Client;
    private static CognitoCachingCredentialsProvider sCredProvider;

    public static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context,
                    Constants.AWS_ACCOUNT_ID,
                    Constants.COGNITO_POOL_ID,
                    Constants.COGNITO_ROLE_UNAUTH,
                    null,
                    Regions.US_EAST_1);
        }
        return sCredProvider;
    }

    public static String getPrefix(Context context) {
        return getCredProvider(context).getIdentityId() + "/";
    }

    public static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            sS3Client = new AmazonS3Client(getCredProvider(context));
        }
        return sS3Client;
    }

    public static String getFileName(String path) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateTmp = sdf.format( new Date() );
        String fileName = String.valueOf(dateTmp)+ "_" + path.substring(path.lastIndexOf("/") + 1) ;
        /*content://com.android.providers.media.documents/document/image:3951
        セミコロンが%表記になってしまうい、aws側で変換されてしまうので、
        アップロードする前に置換
        念のため以下も
        /->%2F	、:->%3A、=->%3D	、?->%3F
        */
        String regex = "%3A|%2F|%3D|%3F";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(fileName);
        String result = m.replaceFirst("Num");

        return result;
    }

    public static boolean doesBucketExist() {
        return sS3Client.doesBucketExist(Constants.BUCKET_NAME.toLowerCase(Locale.US));
    }

    public static void createBucket() {
        sS3Client.createBucket(Constants.BUCKET_NAME.toLowerCase(Locale.US));
    }

    public static void deleteBucket() {
        String name = Constants.BUCKET_NAME.toLowerCase(Locale.US);
        List<S3ObjectSummary> objData = sS3Client.listObjects(name).getObjectSummaries();
        if (objData.size() > 0) {
            DeleteObjectsRequest emptyBucket = new DeleteObjectsRequest(name);
            List<KeyVersion> keyList = new ArrayList<KeyVersion>();
            for (S3ObjectSummary summary : objData) {
                keyList.add(new KeyVersion(summary.getKey()));
            }
            emptyBucket.withKeys(keyList);
            sS3Client.deleteObjects(emptyBucket);
        }
        sS3Client.deleteBucket(name);
    }
}
