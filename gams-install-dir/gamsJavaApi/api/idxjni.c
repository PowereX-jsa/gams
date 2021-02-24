/*  Java Native Interrface code generated by apiwrapper for GAMS Version 33.2.0
 *
 * GAMS - Loading mechanism for GAMS Expert-Level APIs
 *
 * Copyright (c) 2016-2020 GAMS Software GmbH <support@gams.com>
 * Copyright (c) 2016-2020 GAMS Development Corp. <support@gams.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
/* #include <locale.h> */
#include <idxcc.h>

/* at least for some JNI implementations, JNIEXPORT is not setup to
 * explicitly set visibility to default when using GNU compilers;
 * thus, we do this globally for all functions via this pragma
 *  (which is nicer than redefining JNIEXPORT)
 */
#ifdef __GNUC__
#pragma GCC visibility push(default)
#endif

typedef union foo { void *p; jlong i; } u64_t;

typedef char string255[256];
typedef char stringUEL[GLOBAL_UEL_IDENT_SIZE];


static JavaVM *Cached_JVM = NULL;
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
    Cached_JVM = vm;
    idxInitMutexes();
    return JNI_VERSION_1_2;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved)
{
    idxFiniMutexes();
}

/* Prototypes */
JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetReady(JNIEnv *env, jobject obj, jobjectArray msg);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetReadyD(JNIEnv *env, jobject obj, jstring dirName, jobjectArray msg);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetReadyL(JNIEnv *env, jobject obj, jstring libName, jobjectArray msg);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_Create(JNIEnv *env, jobject obj, jobjectArray msg);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_CreateD(JNIEnv *env, jobject obj, jstring dirName, jobjectArray msg);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_CreateL(JNIEnv *env, jobject obj, jstring libName, jobjectArray msg);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_Free(JNIEnv *env, jobject obj);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetLastError(JNIEnv *env, jobject obj);
JNIEXPORT void JNICALL Java_com_gams_api_idx_ErrorStr(JNIEnv *env, jobject obj, jint ErrNr, jobjectArray ErrMsg);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_OpenRead(JNIEnv *env, jobject obj, jstring FileName, jintArray ErrNr);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_OpenWrite(JNIEnv *env, jobject obj, jstring FileName, jstring Producer, jintArray ErrNr);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_Close(JNIEnv *env, jobject obj);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetSymCount(JNIEnv *env, jobject obj, jintArray symCount);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetSymbolInfo(JNIEnv *env, jobject obj, jint iSym, jobjectArray symName, jintArray symDim, jintArray dims, jintArray nNZ, jobjectArray explText);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetSymbolInfoByName(JNIEnv *env, jobject obj, jstring symName, jintArray iSym, jintArray symDim, jintArray dims, jintArray nNZ, jobjectArray explText);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetIndexBase(JNIEnv *env, jobject obj);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_SetIndexBase(JNIEnv *env, jobject obj, jint idxBase);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataReadStart(JNIEnv *env, jobject obj, jstring symName, jintArray symDim, jintArray dims, jintArray nRecs, jobjectArray ErrMsg);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataRead(JNIEnv *env, jobject obj, jintArray keys, jdoubleArray val, jintArray changeIdx);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataReadDone(JNIEnv *env, jobject obj);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataReadSparseColMajor(JNIEnv *env, jobject obj, jint idxBase, jintArray colPtr, jintArray rowIdx, jdoubleArray vals);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataReadSparseRowMajor(JNIEnv *env, jobject obj, jint idxBase, jintArray rowPtr, jintArray colIdx, jdoubleArray vals);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataReadDenseColMajor(JNIEnv *env, jobject obj, jdoubleArray vals);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataReadDenseRowMajor(JNIEnv *env, jobject obj, jdoubleArray vals);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataWriteStart(JNIEnv *env, jobject obj, jstring symName, jstring explTxt, jint symDim, jintArray dims, jobjectArray ErrMsg);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataWrite(JNIEnv *env, jobject obj, jintArray keys, jdouble val);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataWriteDone(JNIEnv *env, jobject obj);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataWriteSparseColMajor(JNIEnv *env, jobject obj, jintArray colPtr, jintArray rowIdx, jdoubleArray vals);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataWriteSparseRowMajor(JNIEnv *env, jobject obj, jintArray rowPtr, jintArray colIdx, jdoubleArray vals);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataWriteDenseColMajor(JNIEnv *env, jobject obj, jint dataDim, jdoubleArray vals);
JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataWriteDenseRowMajor(JNIEnv *env, jobject obj, jint dataDim, jdoubleArray vals);

JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetReady(JNIEnv *env, jobject obj, jobjectArray msg)
{
   int rc_GetReady;
   jstring local_msg;
   char buffer_msg[256];
   rc_GetReady = idxGetReady(buffer_msg, sizeof(buffer_msg));
   local_msg = (*env)->NewStringUTF(env, buffer_msg);
   (*env)->SetObjectArrayElement(env, msg, 0, local_msg);
   return rc_GetReady;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetReadyD(JNIEnv *env, jobject obj, jstring dirName, jobjectArray msg)
{
   int rc_GetReadyD;
   char *local_dirName;
   jstring local_msg;
   char buffer_msg[256];
   buffer_msg[0] = '\0';
   local_dirName = (char *) (*env)->GetStringUTFChars(env, dirName, NULL);
   rc_GetReadyD = idxGetReadyD(local_dirName, buffer_msg, sizeof(buffer_msg));
   (*env)->ReleaseStringUTFChars(env, dirName, local_dirName);
   local_msg = (*env)->NewStringUTF(env, buffer_msg);
   (*env)->SetObjectArrayElement(env, msg, 0, local_msg);
   return rc_GetReadyD;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetReadyL(JNIEnv *env, jobject obj, jstring libName, jobjectArray msg)
{
   int rc_GetReadyL;
   char *local_libName;
   jstring local_msg;
   char buffer_msg[256];
   buffer_msg[0] = '\0';
   local_libName = (char *) (*env)->GetStringUTFChars(env, libName, NULL);
   rc_GetReadyL = idxGetReadyL(local_libName, buffer_msg, sizeof(buffer_msg));
   (*env)->ReleaseStringUTFChars(env, libName, local_libName);
   local_msg = (*env)->NewStringUTF(env, buffer_msg);
   (*env)->SetObjectArrayElement(env, msg, 0, local_msg);
   return rc_GetReadyL;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_Create(JNIEnv *env, jobject obj, jobjectArray msg)
{
   int rc_Create;
   jfieldID fid;
   u64_t pidx;
   jstring local_msg;
   char buffer_msg[256];
   jclass cls = (*env)->GetObjectClass(env, obj);
   buffer_msg[0] = '\0';
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   rc_Create = idxCreate((idxHandle_t *)&pidx.p, buffer_msg, sizeof(buffer_msg));
   local_msg = (*env)->NewStringUTF(env, buffer_msg);
   (*env)->SetObjectArrayElement(env, msg, 0, local_msg);
   (*env)->SetLongField(env, obj, fid, pidx.i);
   return rc_Create;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_CreateD(JNIEnv *env, jobject obj, jstring dirName, jobjectArray msg)
{
   int rc_CreateD;
   jfieldID fid;
   u64_t pidx;
   char *local_dirName;
   jstring local_msg;
   char buffer_msg[256];
   jclass cls = (*env)->GetObjectClass(env, obj);
   buffer_msg[0] = '\0';
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_dirName = (char *) (*env)->GetStringUTFChars(env, dirName, NULL);
   rc_CreateD = idxCreateD((idxHandle_t *)&pidx.p, local_dirName, buffer_msg, sizeof(buffer_msg));
   (*env)->ReleaseStringUTFChars(env, dirName, local_dirName);
   local_msg = (*env)->NewStringUTF(env, buffer_msg);
   (*env)->SetObjectArrayElement(env, msg, 0, local_msg);
   (*env)->SetLongField(env, obj, fid, pidx.i);
   return rc_CreateD;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_CreateL(JNIEnv *env, jobject obj, jstring libName, jobjectArray msg)
{
   int rc_CreateL;
   jfieldID fid;
   u64_t pidx;
   char *local_libName;
   jstring local_msg;
   char buffer_msg[256];
   jclass cls = (*env)->GetObjectClass(env, obj);
   buffer_msg[0] = '\0';
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_libName = (char *) (*env)->GetStringUTFChars(env, libName, NULL);
   rc_CreateL = idxCreateL((idxHandle_t *)&pidx.p, local_libName, buffer_msg, sizeof(buffer_msg));
   (*env)->ReleaseStringUTFChars(env, libName, local_libName);
   local_msg = (*env)->NewStringUTF(env, buffer_msg);
   (*env)->SetObjectArrayElement(env, msg, 0, local_msg);
   (*env)->SetLongField(env, obj, fid, pidx.i);
   return rc_CreateL;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_Free(JNIEnv *env, jobject obj)
{
   int rc_Free;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   rc_Free = idxFree((idxHandle_t *)&pidx.p);
   (*env)->SetLongField(env, obj, fid, pidx.i);
   return rc_Free;
}


JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetLastError(JNIEnv *env, jobject obj)
{
   int rc_idxGetLastError;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   rc_idxGetLastError = idxGetLastError((idxHandle_t)pidx.p);
   return rc_idxGetLastError;
}

JNIEXPORT void JNICALL Java_com_gams_api_idx_ErrorStr(JNIEnv *env, jobject obj, jint ErrNr, jobjectArray ErrMsg)
{
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   jstring local_ErrMsg;
   char buffer_ErrMsg[256];
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return ;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   buffer_ErrMsg[0] = '\0';
   idxErrorStr((idxHandle_t)pidx.p, ErrNr, buffer_ErrMsg, 256);
   local_ErrMsg = (*env)->NewStringUTF(env, buffer_ErrMsg);
   (*env)->SetObjectArrayElement(env, ErrMsg, 0, local_ErrMsg);
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_OpenRead(JNIEnv *env, jobject obj, jstring FileName, jintArray ErrNr)
{
   int rc_idxOpenRead;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   char *local_FileName;
   int local_ErrNr[1];
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_FileName = (char *)(*env)->GetStringUTFChars(env, FileName, NULL);
   rc_idxOpenRead = idxOpenRead((idxHandle_t)pidx.p, local_FileName, local_ErrNr);
   (*env)->ReleaseStringUTFChars(env, FileName, local_FileName);
   (*env)->SetIntArrayRegion(env, ErrNr, 0, 1, local_ErrNr);
   return rc_idxOpenRead;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_OpenWrite(JNIEnv *env, jobject obj, jstring FileName, jstring Producer, jintArray ErrNr)
{
   int rc_idxOpenWrite;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   char *local_FileName;
   char *local_Producer;
   int local_ErrNr[1];
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_FileName = (char *)(*env)->GetStringUTFChars(env, FileName, NULL);
   local_Producer = (char *)(*env)->GetStringUTFChars(env, Producer, NULL);
   rc_idxOpenWrite = idxOpenWrite((idxHandle_t)pidx.p, local_FileName, local_Producer, local_ErrNr);
   (*env)->ReleaseStringUTFChars(env, FileName, local_FileName);
   (*env)->ReleaseStringUTFChars(env, Producer, local_Producer);
   (*env)->SetIntArrayRegion(env, ErrNr, 0, 1, local_ErrNr);
   return rc_idxOpenWrite;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_Close(JNIEnv *env, jobject obj)
{
   int rc_idxClose;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   rc_idxClose = idxClose((idxHandle_t)pidx.p);
   return rc_idxClose;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetSymCount(JNIEnv *env, jobject obj, jintArray symCount)
{
   int rc_idxGetSymCount;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   int local_symCount[1];
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   rc_idxGetSymCount = idxGetSymCount((idxHandle_t)pidx.p, local_symCount);
   (*env)->SetIntArrayRegion(env, symCount, 0, 1, local_symCount);
   return rc_idxGetSymCount;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetSymbolInfo(JNIEnv *env, jobject obj, jint iSym, jobjectArray symName, jintArray symDim, jintArray dims, jintArray nNZ, jobjectArray explText)
{
   int rc_idxGetSymbolInfo;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   jstring local_symName;
   char buffer_symName[256];
   int local_symDim[1];
   int *local_dims;
   int local_nNZ[1];
   jstring local_explText;
   char buffer_explText[256];
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   buffer_symName[0] = '\0';
   local_dims = (*env)->GetIntArrayElements(env, dims, NULL);
   buffer_explText[0] = '\0';
   rc_idxGetSymbolInfo = idxGetSymbolInfo((idxHandle_t)pidx.p, iSym, buffer_symName, 256, local_symDim, local_dims, local_nNZ, buffer_explText, 256);
   local_symName = (*env)->NewStringUTF(env, buffer_symName);
   (*env)->SetObjectArrayElement(env, symName, 0, local_symName);
   (*env)->SetIntArrayRegion(env, symDim, 0, 1, local_symDim);
   (*env)->ReleaseIntArrayElements(env, dims, local_dims, 0);
   (*env)->SetIntArrayRegion(env, nNZ, 0, 1, local_nNZ);
   local_explText = (*env)->NewStringUTF(env, buffer_explText);
   (*env)->SetObjectArrayElement(env, explText, 0, local_explText);
   return rc_idxGetSymbolInfo;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetSymbolInfoByName(JNIEnv *env, jobject obj, jstring symName, jintArray iSym, jintArray symDim, jintArray dims, jintArray nNZ, jobjectArray explText)
{
   int rc_idxGetSymbolInfoByName;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   char *local_symName;
   int local_iSym[1];
   int local_symDim[1];
   int *local_dims;
   int local_nNZ[1];
   jstring local_explText;
   char buffer_explText[256];
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_symName = (char *)(*env)->GetStringUTFChars(env, symName, NULL);
   local_dims = (*env)->GetIntArrayElements(env, dims, NULL);
   buffer_explText[0] = '\0';
   rc_idxGetSymbolInfoByName = idxGetSymbolInfoByName((idxHandle_t)pidx.p, local_symName, local_iSym, local_symDim, local_dims, local_nNZ, buffer_explText, 256);
   (*env)->ReleaseStringUTFChars(env, symName, local_symName);
   (*env)->SetIntArrayRegion(env, iSym, 0, 1, local_iSym);
   (*env)->SetIntArrayRegion(env, symDim, 0, 1, local_symDim);
   (*env)->ReleaseIntArrayElements(env, dims, local_dims, 0);
   (*env)->SetIntArrayRegion(env, nNZ, 0, 1, local_nNZ);
   local_explText = (*env)->NewStringUTF(env, buffer_explText);
   (*env)->SetObjectArrayElement(env, explText, 0, local_explText);
   return rc_idxGetSymbolInfoByName;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_GetIndexBase(JNIEnv *env, jobject obj)
{
   int rc_idxGetIndexBase;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   rc_idxGetIndexBase = idxGetIndexBase((idxHandle_t)pidx.p);
   return rc_idxGetIndexBase;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_SetIndexBase(JNIEnv *env, jobject obj, jint idxBase)
{
   int rc_idxSetIndexBase;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   rc_idxSetIndexBase = idxSetIndexBase((idxHandle_t)pidx.p, idxBase);
   return rc_idxSetIndexBase;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataReadStart(JNIEnv *env, jobject obj, jstring symName, jintArray symDim, jintArray dims, jintArray nRecs, jobjectArray ErrMsg)
{
   int rc_idxDataReadStart;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   char *local_symName;
   int local_symDim[1];
   int *local_dims;
   int local_nRecs[1];
   jstring local_ErrMsg;
   char buffer_ErrMsg[256];
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_symName = (char *)(*env)->GetStringUTFChars(env, symName, NULL);
   local_dims = (*env)->GetIntArrayElements(env, dims, NULL);
   buffer_ErrMsg[0] = '\0';
   rc_idxDataReadStart = idxDataReadStart((idxHandle_t)pidx.p, local_symName, local_symDim, local_dims, local_nRecs, buffer_ErrMsg, 256);
   (*env)->ReleaseStringUTFChars(env, symName, local_symName);
   (*env)->SetIntArrayRegion(env, symDim, 0, 1, local_symDim);
   (*env)->ReleaseIntArrayElements(env, dims, local_dims, 0);
   (*env)->SetIntArrayRegion(env, nRecs, 0, 1, local_nRecs);
   local_ErrMsg = (*env)->NewStringUTF(env, buffer_ErrMsg);
   (*env)->SetObjectArrayElement(env, ErrMsg, 0, local_ErrMsg);
   return rc_idxDataReadStart;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataRead(JNIEnv *env, jobject obj, jintArray keys, jdoubleArray val, jintArray changeIdx)
{
   int rc_idxDataRead;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   int *local_keys;
   double local_val[1];
   int local_changeIdx[1];
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_keys = (*env)->GetIntArrayElements(env, keys, NULL);
   rc_idxDataRead = idxDataRead((idxHandle_t)pidx.p, local_keys, local_val, local_changeIdx);
   (*env)->ReleaseIntArrayElements(env, keys, local_keys, 0);
   (*env)->SetDoubleArrayRegion(env, val, 0, 1, local_val);
   (*env)->SetIntArrayRegion(env, changeIdx, 0, 1, local_changeIdx);
   return rc_idxDataRead;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataReadDone(JNIEnv *env, jobject obj)
{
   int rc_idxDataReadDone;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   rc_idxDataReadDone = idxDataReadDone((idxHandle_t)pidx.p);
   return rc_idxDataReadDone;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataReadSparseColMajor(JNIEnv *env, jobject obj, jint idxBase, jintArray colPtr, jintArray rowIdx, jdoubleArray vals)
{
   int rc_idxDataReadSparseColMajor;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   int *local_colPtr;
   int *local_rowIdx;
   double *local_vals;
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_colPtr = (*env)->GetIntArrayElements(env, colPtr, NULL);
   local_rowIdx = (*env)->GetIntArrayElements(env, rowIdx, NULL);
   local_vals = (*env)->GetDoubleArrayElements(env, vals, NULL);
   rc_idxDataReadSparseColMajor = idxDataReadSparseColMajor((idxHandle_t)pidx.p, idxBase, local_colPtr, local_rowIdx, local_vals);
   (*env)->ReleaseIntArrayElements(env, colPtr, local_colPtr, 0);
   (*env)->ReleaseIntArrayElements(env, rowIdx, local_rowIdx, 0);
   (*env)->ReleaseDoubleArrayElements(env, vals, local_vals, 0);
   return rc_idxDataReadSparseColMajor;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataReadSparseRowMajor(JNIEnv *env, jobject obj, jint idxBase, jintArray rowPtr, jintArray colIdx, jdoubleArray vals)
{
   int rc_idxDataReadSparseRowMajor;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   int *local_rowPtr;
   int *local_colIdx;
   double *local_vals;
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_rowPtr = (*env)->GetIntArrayElements(env, rowPtr, NULL);
   local_colIdx = (*env)->GetIntArrayElements(env, colIdx, NULL);
   local_vals = (*env)->GetDoubleArrayElements(env, vals, NULL);
   rc_idxDataReadSparseRowMajor = idxDataReadSparseRowMajor((idxHandle_t)pidx.p, idxBase, local_rowPtr, local_colIdx, local_vals);
   (*env)->ReleaseIntArrayElements(env, rowPtr, local_rowPtr, 0);
   (*env)->ReleaseIntArrayElements(env, colIdx, local_colIdx, 0);
   (*env)->ReleaseDoubleArrayElements(env, vals, local_vals, 0);
   return rc_idxDataReadSparseRowMajor;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataReadDenseColMajor(JNIEnv *env, jobject obj, jdoubleArray vals)
{
   int rc_idxDataReadDenseColMajor;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   double *local_vals;
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_vals = (*env)->GetDoubleArrayElements(env, vals, NULL);
   rc_idxDataReadDenseColMajor = idxDataReadDenseColMajor((idxHandle_t)pidx.p, local_vals);
   (*env)->ReleaseDoubleArrayElements(env, vals, local_vals, 0);
   return rc_idxDataReadDenseColMajor;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataReadDenseRowMajor(JNIEnv *env, jobject obj, jdoubleArray vals)
{
   int rc_idxDataReadDenseRowMajor;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   double *local_vals;
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_vals = (*env)->GetDoubleArrayElements(env, vals, NULL);
   rc_idxDataReadDenseRowMajor = idxDataReadDenseRowMajor((idxHandle_t)pidx.p, local_vals);
   (*env)->ReleaseDoubleArrayElements(env, vals, local_vals, 0);
   return rc_idxDataReadDenseRowMajor;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataWriteStart(JNIEnv *env, jobject obj, jstring symName, jstring explTxt, jint symDim, jintArray dims, jobjectArray ErrMsg)
{
   int rc_idxDataWriteStart;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   char *local_symName;
   char *local_explTxt;
   int *local_dims;
   jstring local_ErrMsg;
   char buffer_ErrMsg[256];
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_symName = (char *)(*env)->GetStringUTFChars(env, symName, NULL);
   local_explTxt = (char *)(*env)->GetStringUTFChars(env, explTxt, NULL);
   local_dims = (*env)->GetIntArrayElements(env, dims, NULL);
   buffer_ErrMsg[0] = '\0';
   rc_idxDataWriteStart = idxDataWriteStart((idxHandle_t)pidx.p, local_symName, local_explTxt, symDim, local_dims, buffer_ErrMsg, 256);
   (*env)->ReleaseStringUTFChars(env, symName, local_symName);
   (*env)->ReleaseStringUTFChars(env, explTxt, local_explTxt);
   (*env)->ReleaseIntArrayElements(env, dims, local_dims, 0);
   local_ErrMsg = (*env)->NewStringUTF(env, buffer_ErrMsg);
   (*env)->SetObjectArrayElement(env, ErrMsg, 0, local_ErrMsg);
   return rc_idxDataWriteStart;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataWrite(JNIEnv *env, jobject obj, jintArray keys, jdouble val)
{
   int rc_idxDataWrite;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   int *local_keys;
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_keys = (*env)->GetIntArrayElements(env, keys, NULL);
   rc_idxDataWrite = idxDataWrite((idxHandle_t)pidx.p, local_keys, val);
   (*env)->ReleaseIntArrayElements(env, keys, local_keys, 0);
   return rc_idxDataWrite;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataWriteDone(JNIEnv *env, jobject obj)
{
   int rc_idxDataWriteDone;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   rc_idxDataWriteDone = idxDataWriteDone((idxHandle_t)pidx.p);
   return rc_idxDataWriteDone;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataWriteSparseColMajor(JNIEnv *env, jobject obj, jintArray colPtr, jintArray rowIdx, jdoubleArray vals)
{
   int rc_idxDataWriteSparseColMajor;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   int *local_colPtr;
   int *local_rowIdx;
   double *local_vals;
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_colPtr = (*env)->GetIntArrayElements(env, colPtr, NULL);
   local_rowIdx = (*env)->GetIntArrayElements(env, rowIdx, NULL);
   local_vals = (*env)->GetDoubleArrayElements(env, vals, NULL);
   rc_idxDataWriteSparseColMajor = idxDataWriteSparseColMajor((idxHandle_t)pidx.p, local_colPtr, local_rowIdx, local_vals);
   (*env)->ReleaseIntArrayElements(env, colPtr, local_colPtr, 0);
   (*env)->ReleaseIntArrayElements(env, rowIdx, local_rowIdx, 0);
   (*env)->ReleaseDoubleArrayElements(env, vals, local_vals, 0);
   return rc_idxDataWriteSparseColMajor;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataWriteSparseRowMajor(JNIEnv *env, jobject obj, jintArray rowPtr, jintArray colIdx, jdoubleArray vals)
{
   int rc_idxDataWriteSparseRowMajor;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   int *local_rowPtr;
   int *local_colIdx;
   double *local_vals;
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_rowPtr = (*env)->GetIntArrayElements(env, rowPtr, NULL);
   local_colIdx = (*env)->GetIntArrayElements(env, colIdx, NULL);
   local_vals = (*env)->GetDoubleArrayElements(env, vals, NULL);
   rc_idxDataWriteSparseRowMajor = idxDataWriteSparseRowMajor((idxHandle_t)pidx.p, local_rowPtr, local_colIdx, local_vals);
   (*env)->ReleaseIntArrayElements(env, rowPtr, local_rowPtr, 0);
   (*env)->ReleaseIntArrayElements(env, colIdx, local_colIdx, 0);
   (*env)->ReleaseDoubleArrayElements(env, vals, local_vals, 0);
   return rc_idxDataWriteSparseRowMajor;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataWriteDenseColMajor(JNIEnv *env, jobject obj, jint dataDim, jdoubleArray vals)
{
   int rc_idxDataWriteDenseColMajor;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   double *local_vals;
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_vals = (*env)->GetDoubleArrayElements(env, vals, NULL);
   rc_idxDataWriteDenseColMajor = idxDataWriteDenseColMajor((idxHandle_t)pidx.p, dataDim, local_vals);
   (*env)->ReleaseDoubleArrayElements(env, vals, local_vals, 0);
   return rc_idxDataWriteDenseColMajor;
}

JNIEXPORT jint JNICALL Java_com_gams_api_idx_DataWriteDenseRowMajor(JNIEnv *env, jobject obj, jint dataDim, jdoubleArray vals)
{
   int rc_idxDataWriteDenseRowMajor;
   jfieldID fid;
   u64_t pidx;
   jclass cls = (*env)->GetObjectClass(env, obj);
   double *local_vals;
   fid = (*env)->GetFieldID(env, cls, "idxPtr", "J");
   if (fid == NULL) return 0;
   pidx.i = (*env)->GetLongField(env, obj, fid);
   local_vals = (*env)->GetDoubleArrayElements(env, vals, NULL);
   rc_idxDataWriteDenseRowMajor = idxDataWriteDenseRowMajor((idxHandle_t)pidx.p, dataDim, local_vals);
   (*env)->ReleaseDoubleArrayElements(env, vals, local_vals, 0);
   return rc_idxDataWriteDenseRowMajor;
}
