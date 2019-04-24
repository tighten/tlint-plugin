package tlint.cli

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import java.util.ArrayList

class TLint {
    var file = File()

    class File {
        var errors: List<Issue> = ArrayList()
    }

    class Issue {
        var source: String? = null
        var line: Int = 0
        var message: String? = null
    }

    companion object {

        internal fun read(json: String): TLint {
            val lint = TLint()

            try {
                val builder = GsonBuilder()
                val gson = builder.create()
                lint.file = gson.fromJson(json, File::class.java)
            } catch (e: JsonSyntaxException) {
                //
            } catch (e: java.lang.IllegalStateException) {
                //
            }

            return lint
        }
    }
}