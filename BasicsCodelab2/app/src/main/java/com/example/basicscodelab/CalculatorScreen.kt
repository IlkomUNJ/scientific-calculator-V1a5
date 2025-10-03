package com.example.basicscodelab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import android.R.attr.text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object CalculatorScreen {
    @Composable
    fun SetupLayout(){
        val viewModel = remember { CalculatorViewModel() }

        val state = viewModel.state
        val mainDisplayText = state.result ?: state.expression

        val clearButtonText = viewModel.getClearButtonText()

        var displayFontSize = 40.sp
        if (mainDisplayText.length > 10) {
            displayFontSize = 30.sp
        }
        if (mainDisplayText.length > 15) {
            displayFontSize = 25.sp
        }
        if (mainDisplayText.length > 20) {
            displayFontSize = 15.sp
        }

        var expanded by remember { mutableStateOf(false) }
        Surface (
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,

        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                ExpandButton(
                    expanded = expanded,
                    onClick = { expanded = !expanded },
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                )
                Column (
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ){
                    Text(
                        text = if (state.isResultDisplayed) state.expression else "",
                        fontSize = 40.sp,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.End)
                            .padding(end = 25.dp)
                    )
                    Text(
                        text = mainDisplayText,
                        fontSize = displayFontSize,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.End)
                            .padding(end = 25.dp)

                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.SpaceAround
                            ){
                                AnimatedVisibility(
                                    visible = expanded,
                                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                                ){
                                    Column{
                                            Row{
                                                CalculatorButtonExpand(text = "sin", onClick = { viewModel.onButtonClick("sin") }, Modifier.padding(4.dp))
                                                CalculatorButtonExpand(text = "cos", onClick = { viewModel.onButtonClick("cos") }, Modifier.padding(4.dp))
                                                CalculatorButtonExpand(text = "tan", onClick = { viewModel.onButtonClick("tan") }, Modifier.padding(4.dp))
                                                CalculatorButtonExpand(text = "sin⁻¹", onClick = { viewModel.onButtonClick("sin⁻¹") }, Modifier.padding(4.dp))
                                                CalculatorButtonExpand(text = "cos⁻¹", onClick = { viewModel.onButtonClick("cos⁻¹") }, Modifier.padding(4.dp))
                                                CalculatorButtonExpand(text = "tan⁻¹", onClick = { viewModel.onButtonClick("tan⁻¹") }, Modifier.padding(4.dp))
                                            }
                                            Row{
                                                CalculatorButtonExpand(text = "x!", onClick = { viewModel.onButtonClick("x!") }, Modifier.padding(4.dp))
                                                CalculatorButtonExpand(text = "¹/x", onClick = { viewModel.onButtonClick("¹/x") }, Modifier.padding(4.dp))
                                                CalculatorButtonExpand(text = "xʸ", onClick = { viewModel.onButtonClick("xʸ") }, Modifier.padding(4.dp))
                                                CalculatorButtonExpand(text = "√x", onClick = { viewModel.onButtonClick("√x") }, Modifier.padding(4.dp))
                                                CalculatorButtonExpand(text = "log", onClick = { viewModel.onButtonClick("log") }, Modifier.padding(4.dp))
                                                CalculatorButtonExpand(text = "ln", onClick = { viewModel.onButtonClick("ln") }, Modifier.padding(4.dp))
                                            }
                                    }
                                }
                            }

                            Row {
                                CalculatorButton("7", Modifier.padding(4.dp), onClick = { viewModel.onButtonClick("7") })
                                CalculatorButton("8", Modifier.padding(4.dp), onClick = { viewModel.onButtonClick("8") })
                                CalculatorButton("9", Modifier.padding(4.dp), onClick = { viewModel.onButtonClick("9") })
                                CalculatorButton("DEL", Modifier.padding(4.dp), fontSize = 15, color = Color(0xFFFF00FF), onClick = { viewModel.onButtonClick("DEL") })
                                CalculatorButton(text = clearButtonText, Modifier.padding(4.dp), fontSize = 15, color = Color(0xFFFF00FF), onClick = { viewModel.onButtonClick("AC") })
                            }
                            Row {
                                CalculatorButton("4", Modifier.padding(4.dp), onClick = { viewModel.onButtonClick("4") })
                                CalculatorButton("5", Modifier.padding(4.dp), onClick = { viewModel.onButtonClick("5") })
                                CalculatorButton("6", Modifier.padding(4.dp), onClick = { viewModel.onButtonClick("6") })
                                CalculatorButton("x", Modifier.padding(4.dp), color = Color(0xFF4169E1), onClick = { viewModel.onButtonClick("x") })
                                CalculatorButton("/", Modifier.padding(4.dp), color = Color(0xFF4169E1), onClick = { viewModel.onButtonClick("/") })
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column {

                                    Row {
                                        CalculatorButton("1", Modifier.padding(4.dp), onClick = { viewModel.onButtonClick("1") })
                                        CalculatorButton("2", Modifier.padding(4.dp), onClick = { viewModel.onButtonClick("2") })
                                        CalculatorButton("3", Modifier.padding(4.dp), onClick = { viewModel.onButtonClick("3") })
                                        CalculatorButton("+", Modifier.padding(4.dp), color = Color(0xFF4169E1), onClick = { viewModel.onButtonClick("+") })
                                    }
                                    Row {
                                        CalculatorButton("e", Modifier.padding(4.dp), onClick = { viewModel.onButtonClick("e") })
                                        CalculatorButton("0", Modifier.padding(4.dp), onClick = { viewModel.onButtonClick("0") })
                                        CalculatorButton(".", Modifier.padding(4.dp), onClick = { viewModel.onButtonClick(".") })
                                        CalculatorButton("-", Modifier.padding(4.dp), color = Color(0xFF4169E1), onClick = { viewModel.onButtonClick("-") })
                                    }
                                }
                                TallCalculatorButton("=", Modifier.padding(4.dp), color = Color(0xFF4169E1), onClick = { viewModel.onButtonClick("=") })
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CalculatorButtonExpand(
        text : String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        fontSize : Int = 15,
        color: Color = Color(0xFF4B0082),
        textColor: Color = Color.White){
        ElevatedButton(
            onClick = onClick,
            modifier = modifier.width(48.5.dp).height(50.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp) ,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = color,
                contentColor = textColor
            )
//            80 hp 90 privew
        ){
            Text(text = text, fontSize = fontSize.sp, color = textColor)

        }
    }

    @Composable
    fun ExpandButton(
        expanded: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        ElevatedButton(
            onClick = onClick,
            modifier = modifier.width(36.dp).height(36.dp),
            shape = RectangleShape,
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(0.dp) /
        ) {
            Icon(
                imageVector = if (expanded) Filled.ExpandLess else Filled.ExpandMore,
                contentDescription = if (expanded) "Collapse scientific panel" else "Expand scientific panel",
                tint = Color.White,modifier = Modifier.size(50.dp)
            )
        }
    }

    @Composable
    fun CalculatorButton(
        text : String,
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
        fontSize : Int = 20,
        color: Color = Color(0xFF2D2D2D),
        textColor: Color = Color.White){
        ElevatedButton(
            onClick = onClick,
            modifier = modifier.width(60.dp).height(50.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp) ,
            colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = color,
            contentColor = textColor
            )
        ){
            Text(text = text, fontSize = fontSize.sp, color = textColor)

        }
    }

    @Composable
    fun TallCalculatorButton(
        text: String,
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
        color: Color = Color.White,   
        textColor: Color = Color.White) {
        ElevatedButton(
            onClick = onClick,
            modifier = modifier.width(60.dp).height(108.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = color,
                contentColor = textColor
            )
        ) {
            Text(text = text, fontSize = 30.sp, color = textColor)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CalculatorPreview() {
    CalculatorScreen.SetupLayout()
}

