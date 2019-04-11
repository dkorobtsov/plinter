class SonarConfig {

    companion object {
        fun coverageExclusions(): String {
            return arrayOf(
                    "**/CacheControl.java",
                    "**/HttpHeaders.java",
                    "**/InterceptedHeaders.java",
                    "**/HttpMethod.java",
                    "**/InterceptedMediaType.java",
                    "**/InterceptedRequest.java",
                    "**/InterceptedRequestBody.java",
                    "**/InterceptedResponseBody.java",
                    "**/Protocol.java",
                    "**/Util.java"
            ).joinToString(separator = ", ")
        }

        fun duplicationExclusions(): String {
            return arrayOf(
                    "**/okhttp/**",
                    "**/okhttp3/**"
            ).joinToString(separator = ", ")
        }

        fun sonarExclusions(): String {
            return arrayOf(
                    "**/CacheControl.java",
                    "**/InterceptedRequestBody.java",
                    "**/InterceptedHeaders.java",
                    "**/InterceptedMediaType.java",
                    "**/Protocol.java",
                    "**/Util.java"
            ).joinToString(separator = ", ")
        }
    }

}