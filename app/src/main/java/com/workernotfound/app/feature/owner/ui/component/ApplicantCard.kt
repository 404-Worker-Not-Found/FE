package com.workernotfound.app.feature.owner.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workernotfound.app.core.designsystem.AppColors
import com.workernotfound.app.core.designsystem.AppTheme
import com.workernotfound.app.core.designsystem.component.AppBadge
import com.workernotfound.app.core.designsystem.component.AppCard
import com.workernotfound.app.core.designsystem.component.AppStatus
import com.workernotfound.app.core.designsystem.component.NoShowRiskBadge
import com.workernotfound.app.core.designsystem.component.StatusBadge
import com.workernotfound.app.feature.owner.domain.model.Applicant

/**
 * 지원자 카드 (UI spec 2-3): 프로필 이미지, 이름, 평점, 예상 도착 시간, 매칭 점수,
 * 노쇼 예측 뱃지, 업종 경험 아이콘.
 */
@Composable
fun ApplicantCard(
    applicant: Applicant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(name = applicant.name, imageUrl = applicant.profileImageUrl)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                NameRow(applicant)
                Spacer(Modifier.height(4.dp))
                RatingRow(applicant)
            }
            MatchScore(score = applicant.matchScore)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            NoShowRiskBadge(risk = applicant.noShowRisk.toBadge())
            Spacer(Modifier.width(6.dp))
            if (applicant.isMatched) StatusBadge(status = AppStatus.MATCHED)
            Spacer(Modifier.weight(1f))
            ExperienceMark(hasExperience = applicant.hasCategoryExperience)
        }
    }
}

@Composable
private fun NameRow(applicant: Applicant) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = applicant.name,
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.TextMain,
        )
        Spacer(Modifier.width(6.dp))
        Text(text = "${applicant.age}세", color = AppColors.TextSub, fontSize = 12.sp)
    }
}

@Composable
private fun RatingRow(applicant: Applicant) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "평점",
            tint = AppColors.Warning,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(2.dp))
        Text(text = "%.1f".format(applicant.rating), color = AppColors.TextMain, fontSize = 13.sp)
        Text(text = " · 도착 예상 ${applicant.etaMinutes}분", color = AppColors.TextSub, fontSize = 13.sp)
    }
}

@Composable
private fun MatchScore(score: Int) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = "$score",
            color = AppTheme.role.accent,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(text = "매칭 점수", color = AppColors.TextSub, fontSize = 11.sp)
    }
}

/** 업종 경험 표시: 해당 업종 근무 이력 여부 아이콘. */
@Composable
internal fun ExperienceMark(hasExperience: Boolean, modifier: Modifier = Modifier) {
    if (hasExperience) {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.WorkHistory,
                contentDescription = "업종 경험 있음",
                tint = AppColors.Success,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(text = "업종 경험", color = AppColors.Success, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        AppBadge(
            text = "업종 경험 없음",
            background = AppColors.Border,
            contentColor = AppColors.TextSub,
            modifier = modifier,
        )
    }
}
