# Android Secure Storage

[![Download](https://api.bintray.com/packages/jcloquell/Maven/android-secure-storage/images/download.svg)](https://bintray.com/jcloquell/Maven/android-secure-storage/_latestVersion) [![API](https://img.shields.io/badge/API-18%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=18) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Why?

As many other Android libraries out there, Android Secure Storage can be used for storing data in the shared preferences in a secure way.
The reason why I decided to create this library is because I experienced different issues trying to encrypt large amounts of data following the different answers I could find online. In the end, combining some of these sources did the trick.
Also, mostly all the existing similar libraries are wrappers around the Android SharedPreferences API, which is great, but they are limited to mainly store primitive types. Android Secure Storage uses the default SharedPreferences internally, but offers a different interface that lets you store any kind of object (primitive or not), as large as necessary, in an easy way.

## How?

Depending on the API level, a different algorithm is used to generate the secret key that will be used to encrypt/decrypt the data before saving it in the shared preferences.
- From Android 6.0 (API 23) onwards: a symmetric secret key is generated using the Android KeyStore with AES, CBC, and PKCS7 padding.
- From Android 4.3 (API 18) onwards: a symmetric secret key is generated (AES) and encrypted with an asymmetric key using RSA, ECB, and PKCS1 padding, and then saved in SharedPreferences.
In both cases, the generated secret key is then used by a symmetric cipher which uses a AES, CBC and PKCS7 padding transformation, with a randomly generated Initialization Vector (IV), to finally encrypt/decrypt the data.

## Download

- **Gradle**
```
implementation 'com.jcloquell:android-secure-storage:latest_version'
```

- **Maven**
```
<dependency>
  <groupId>com.jcloquell</groupId>
  <artifactId>android-secure-storage</artifactId>
  <version>latest_version</version>
  <type>pom</type>
</dependency>
```

## How to use

There is only one class you should worry about, called `SecureStorage` and it's pretty simple to use. To create a new instance a `Context` needs to be provided:
```kotlin
val secureStorage = SecureStorage(context)
```

In order to store an object of any type (might be a primitive type or a custom POJO), you need to pass a key (String) and the object you want to securely store. For example, to store an Integer:
```kotlin
secureStorage.storeObject("integerKey", 100)
```

And for storing a more complex object:
```kotlin
val complexObject = ComplexObject()
secureStorage.storeObject("complexObjectKey", complexObject)
```

To get the securely stored objects, normally you just need to pass the same key that was used when storing them and the Class of the object you want to get. For example, to get an Integer:

Kotlin:
```kotlin
val decryptedInteger = secureStorage.getObject("integerKey", Int::class.java)
```
Java:
```java
Integer decryptedInteger = secureStorage.getObject("integerKey", Integer.class);
```

And for getting a more complex object:

Kotlin:
```kotlin
val decryptedComplexObject = secureStorage.getObject("complexObjectKey", ComplexObject::class.java)
```
Java:
```java
ComplexObject decryptedComplexObject = secureStorage.getObject("complexObjectKey", ComplexObject.class);
```

It's also possible to store (same than above) and get Collections (List, HashMap, Set...) of objects by passing the Type instead of the Class to the same overloaded method, like this:

Kotlin:
```kotlin
val decryptedList = secureStorage.getObject<List<Object>>("listKey", object : TypeToken<List<Object>>() {}.type)
```
Java:
```java
List<Object> decryptedList = secureStorage.getObject("complexObjectKey", new TypeToken<List<Object>>() {}.getType());
```

The syntax is a bit ugly because it's using Gson internally, and there are some limitations for Collections. Check the limitations section below.

In case the object you are trying to get with the provided key doesn't exist, a `null` value will be returned.

You can also check if some object is already stored for a given key:
```kotlin
val isStored = secureStorage.containsObject("key")
```

And also remove objects from the storage passing the proper key:
```kotlin
secureStorage.removeObject("key")
```

**Additionally** the `EncryptionHelper` class can also be used to encrypt/decrypt Strings on your own, in case you want to have more flexibility. You just need to pass a key and the String to be encrypted/decrypted. And for creating a new instance, again, you only need to provide a Context:
```kotlin
val encryptionHelper = EncryptionHelper(context)

val encryptedString = encryptionHelper.encrypt(key, "this is a String")

val decryptedString = encryptionHelper.decrypt(key, encryptedString)
```

## Limitations

In order to encrypt Collections of objects (List, HashMap, Set...), [Gson](https://github.com/google/gson) is required, as the `Type` of the object needs to be passed as a parameter to the `SecureStorage`. To get the `Type`, the `TypeToken` class (which is included in the Gson library) is necessary, as shown in the example above. You could also get the Type using reflection, but it's not a recommended practice.

If you don't use Gson in your project and don't want to add it as a dependency, you will need to wrap your Collection with a custom POJO to be able to encrypt it. In the sample app, there is an example of this, where a custom data class (`HeavyObject`) contains an Array of Strings.

Also, if you are already using Gson in your project and the version differs from the one used internally in this library, to avoid version conflicts, you probably want to exclude the gson module when adding the library as a dependency in your `build.gradle` file.

## Contributors

[@jcloquell](https://github.com/jcloquell)

[@elaydis](https://github.com/elaydis)


## License

```
    Copyright 2018 Jorge Cloquell Ribera, Nadine Kost

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
```