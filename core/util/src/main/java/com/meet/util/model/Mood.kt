package com.meet.util.model

import androidx.compose.ui.graphics.Color
import com.example.util.R
import com.meet.ui.theme.AngryColor
import com.meet.ui.theme.AwfulColor
import com.meet.ui.theme.BoredColor
import com.meet.ui.theme.CalmColor
import com.meet.ui.theme.DepressedColor
import com.meet.ui.theme.DisappointedColor
import com.meet.ui.theme.HappyColor
import com.meet.ui.theme.HumorousColor
import com.meet.ui.theme.LonelyColor
import com.meet.ui.theme.MysteriousColor
import com.meet.ui.theme.NeutralColor
import com.meet.ui.theme.RomanticColor
import com.meet.ui.theme.ShamefulColor
import com.meet.ui.theme.SurprisedColor
import com.meet.ui.theme.SuspiciousColor
import com.meet.ui.theme.TenseColor


enum class Mood(
    val icon: Int,
    val contentColor: Color,
    val containerColor: Color,
) {
    Neutral(
        icon = R.drawable.neutral,
        contentColor = Color.Black,
        containerColor = NeutralColor
    ),
    Angry(
        icon = R.drawable.angry,
        contentColor = Color.White,
        containerColor = AngryColor
    ),
    Happy(
        icon = R.drawable.happy,
        contentColor = Color.Black,
        containerColor = HappyColor
    ),
    Bored(
    icon = R.drawable.bored,
    contentColor = Color.Black,
    containerColor = BoredColor
    ),
    Calm(
    icon = R.drawable.calm,
    contentColor = Color.Black,
    containerColor = CalmColor
    ),
    Depressed(
    icon = R.drawable.depressed,
    contentColor = Color.Black,
    containerColor = DepressedColor
    ),
    Disappointed(
    icon = R.drawable.disappointed,
    contentColor = Color.White,
    containerColor = DisappointedColor
    ),
    Humorous(
    icon = R.drawable.humorous,
    contentColor = Color.Black,
    containerColor = HumorousColor
    ),
    Lonely(
    icon = R.drawable.lonely,
    contentColor = Color.White,
    containerColor = LonelyColor
    ),
    Mysterious(
    icon = R.drawable.mysterious,
    contentColor = Color.Black,
    containerColor = MysteriousColor
    ),
    Romantic(
    icon = R.drawable.romantic,
    contentColor = Color.White,
    containerColor = RomanticColor
    ),
    Shameful(
    icon = R.drawable.shameful,
    contentColor = Color.White,
    containerColor = ShamefulColor
    ),
    Awful(
    icon = R.drawable.awful,
    contentColor = Color.Black,
    containerColor = AwfulColor
    ),
    Surprised(
    icon = R.drawable.surprised,
    contentColor = Color.Black,
    containerColor = SurprisedColor
    ),
    Suspicious(
    icon = R.drawable.suspicious,
    contentColor = Color.Black,
    containerColor = SuspiciousColor
    ),
    Tense(
    icon = R.drawable.tense,
    contentColor = Color.Black,
    containerColor = TenseColor
    )
}