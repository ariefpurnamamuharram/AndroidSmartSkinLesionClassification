package id.ariefpurnamamuharram.skinlesionclassificationbyai.learningcenter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.ariefpurnamamuharram.skinlesionclassificationbyai.R

class LearningCenterFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_learning_center, container, false)

        return rootView
    }

    companion object {
        fun newInstance(): LearningCenterFragment = LearningCenterFragment()
    }

}