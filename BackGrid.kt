package lib.book.refLibrary

/**
 * Created by Administrator on 2018/1/7.
 */

class BackGrid(){
    companion object {
        inline fun buildKey(rowInd: Int, colInd: Int, type: Int = 0)
                = (rowInd shl 14) or colInd
        const val MaxInd = (1 shl 14) - 1
        const val InValid = -1
        inline val Int.rowInd: Int
            get() = (this ushr 14) and MaxInd
        inline val Int.colInd: Int
            get() = this and MaxInd
        inline fun keyToString(key: Int) = "(${key.rowInd},${key.colInd})"
        //[0,maxValue], [a1,a2)
        inline fun boundUpper(a:Int, b:Int, value:Int,maxValue:Int):Int {
            if (value < a + b) return 0
            var result = value / (a + b)

            if (result > maxValue) return InValid
            return result
        }
        //[0,maxValue], (a1,a2]
        inline fun boundLower(a:Int, b:Int, value:Int,maxValue:Int):Int {
            if (value <= a) return InValid
            var adValue = value-a
            var result = adValue/(a+b)
            if(adValue.rem(a+b)==0) result--
            if(result>maxValue) result = maxValue
            return result
        }
    }
}