package com.example.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeTopBar(
    onMenuClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    dateIsSelected: Boolean,
    onFromDateSelected: (ZonedDateTime) -> Unit,
    onToDateSelected: (ZonedDateTime) -> Unit,
    onDateReset: () -> Unit
) {
    val fromDateDialog = rememberSheetState()
    val toDateDialog = rememberSheetState()
    var pickedFromDate by remember {
        mutableStateOf(LocalDate.now())
    }
    var pickedToDate by remember {
        mutableStateOf(LocalDate.now())
    }
    TopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = onMenuClicked) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Hamburger Menu Icon"
                )
            }
        },
        title = {
            Text(text = "Diary")
        },
        actions = {
            if (dateIsSelected) {
                IconButton(onClick = onDateReset) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Close Icon"
                    )
                }
            } else {
                IconButton(onClick = { fromDateDialog.show() }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Date Icon"
                    )
                }
            }
        }
    )
    
    CalendarDialog(
        state = fromDateDialog,
        header = Header.Default(title = "Select From Date"),
        selection = CalendarSelection.Date { localDate ->
            pickedFromDate = localDate
            onFromDateSelected(
                ZonedDateTime.of(
                    pickedFromDate,
                    LocalTime.now(),
                    ZoneId.systemDefault()
                )
            )
            toDateDialog.show()
        },
        config = CalendarConfig(monthSelection = true, yearSelection = true)
    )
    CalendarDialog(
        state = toDateDialog,
        header = Header.Default(title = "Select To Date"),
        selection = CalendarSelection.Date { localDate ->
            pickedToDate = localDate
            onToDateSelected(
                ZonedDateTime.of(
                    pickedToDate,
                    LocalTime.now(),
                    ZoneId.systemDefault()
                )
            )
        },
        config = CalendarConfig(monthSelection = true, yearSelection = true)
    )
}