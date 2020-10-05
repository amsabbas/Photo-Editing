package akapps.photoediting.photocollage.viewmodel

import akapps.photoediting.base.model.Resource
import akapps.photoediting.base.viewmodel.BaseViewModel
import akapps.photoediting.extension.setError
import akapps.photoediting.extension.setLoading
import akapps.photoediting.extension.setSuccess
import akapps.photoediting.photocollage.model.Coordinate
import akapps.photoediting.photocollage.model.Pixel
import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*


class PhotoCollageViewModel : BaseViewModel() {

    val divideLiveData = MutableLiveData<Resource<Boolean>>()

    val segments = ArrayList<Queue<Pixel>>()
    val xySegments = ArrayList<Coordinate>()

    fun divideImage(bitmap: Bitmap) {
        compositeDisposable.add(Single.fromCallable {
            divideImgIntoSegments(bitmap)
        }.subscribeOn(Schedulers.newThread()).doOnSubscribe { divideLiveData.setLoading() }
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    divideLiveData.setSuccess(true)
                }, {
                    divideLiveData.setError()
                }))


    }


    private fun divideImgIntoSegments(bitmap: Bitmap) {
        val visited = Array(bitmap.width) { BooleanArray(bitmap.height) }
        for (i in 0 until bitmap.width) {
            for (j in 0 until bitmap.height) {
                visited[i][j] = false
            }
        }

        for (i in 0 until bitmap.width) {
            for (j in 0 until bitmap.height) {
                val transparencyPixel = bitmap.getPixel(i, j)

                if (Color.alpha(transparencyPixel) != 0 && !visited[i][j]) {
                    var minX = 10000
                    var maxX = 0
                    var minY = 10000
                    var maxY = 0
                    val queue = LinkedList<Pixel>()
                    val tmpQueue = LinkedList<Pixel>()

                    queue.add(Pixel(i, j))
                    visited[i][j] = true
                    while (!queue.isEmpty()) {
                        val currPixel = queue.peek()

                            if (minX > currPixel!!.x)
                                minX = currPixel.x

                            if (maxX < currPixel.x)
                                maxX = currPixel.x

                            if (minY > currPixel.y)
                                minY = currPixel.y

                            if (maxY < currPixel.y)
                                maxY = currPixel.y

                            tmpQueue.add(currPixel)
                            queue.poll()

                            if (currPixel.y + 1 < bitmap.height && !visited[currPixel.x][currPixel.y + 1]) {
                                val transparency = bitmap.getPixel(currPixel.x, currPixel.y + 1)
                                if (Color.alpha(transparency) != 0) {
                                    queue.add(Pixel(currPixel.x, currPixel.y + 1))
                                    visited[currPixel.x][currPixel.y + 1] = true

                                }
                            }
                            if (currPixel.y - 1 >= 0 && !visited[currPixel.x][currPixel.y - 1]) {
                                val transparency = bitmap.getPixel(currPixel.x, currPixel.y - 1)
                                if (Color.alpha(transparency) != 0) {
                                    queue.add(Pixel(currPixel.x, currPixel.y - 1))
                                    visited[currPixel.x][currPixel.y - 1] = true
                                }
                            }
                            if (currPixel.x + 1 < bitmap.width && !visited[currPixel.x + 1][currPixel.y]) {
                                val transparency = bitmap.getPixel(currPixel.x + 1, currPixel.y)
                                if (Color.alpha(transparency) != 0) {
                                    queue.add(Pixel(currPixel.x + 1, currPixel.y))
                                    visited[currPixel.x + 1][currPixel.y] = true
                                }
                            }
                            if (currPixel.x - 1 >= 0 && !visited[currPixel.x - 1][currPixel.y]) {
                                val transparency = bitmap.getPixel(currPixel.x - 1, currPixel.y)
                                if (Color.alpha(transparency) != 0) {
                                    queue.add(Pixel(currPixel.x - 1, currPixel.y))
                                    visited[currPixel.x - 1][currPixel.y] = true
                                }
                            }
                        }
                        segments.add(tmpQueue)
                        xySegments.add(Coordinate(minX, maxX, minY, maxY))

                }
            }
        }
    }

}