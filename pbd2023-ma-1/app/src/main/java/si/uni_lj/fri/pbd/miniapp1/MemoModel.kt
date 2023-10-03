package si.uni_lj.fri.pbd.miniapp1

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import android.util.Base64
import org.json.JSONObject

data class MemoModel(
    val timestamp : String?,
    val title : String,
    val image : Bitmap?,
    val description : String
) {

    /**
     * Returns json string
     */
    fun toJson(): String {
        var json = JSONObject()
        val stringImage = bitmapToString(this.image)
        json.put("timestamp", this.timestamp)
        json.put("title", this.title)
        json.put("image", stringImage)
        json.put("description", this.description)
        return json.toString()
    }

    /**
     * Returns base64 encoded string of bitmap image
     * help: https://stackoverflow.com/questions/30818538/converting-json-object-with-bitmaps
     */
    private fun bitmapToString(bitmapPicture: Bitmap?): String? {
        val encodedImage: String
        val byteArrayBitmapStream = ByteArrayOutputStream()
        bitmapPicture?.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayBitmapStream)
        val b: ByteArray = byteArrayBitmapStream.toByteArray()
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT)
        return encodedImage
    }
}
