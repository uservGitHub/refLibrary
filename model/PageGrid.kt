package lib.book.refLibrary.model

import android.graphics.Color

/**
 * Created by work on 2018/1/9.
 */

data class PageGrid(var backGridId:Int,
                    var pageHorSize:Int,
                    var pageVerSize:Int,
                    var pagePerWidth:Int,
                    var pagePerHeight:Int,
                    var pageCols:Int,
                    var pageRows:Int,
                    var pageMarginColor:Int)