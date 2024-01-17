package club.endi.endihub.util

import de.themoep.minedown.adventure.MineDown
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecorationAndState

val colors = mapOf(
    "&(primary-1)" to "&#913AFF&",
    "&(primary-2)" to "&#FF3AEB&",
    "&(primary-3)" to "&#945DDB&",
    "&(primary-gradient)" to "&#913AFF-#FF3AEB&",

    "&(energy)" to "&#FF9900&",
    "&(time)" to "&#00FFA3&",

    "&(gray-1)" to "&#AAAAAA&",
    "&(gray-2)" to "&#555555&",

    "&(err)" to "&#FF3D00&",
    "&(success)" to "&#8FFF00&",
    "&(warn)" to "&#FFE500&",
)

class Text {
    companion object {
        const val prefix = "**&#913AFF-#FF3AEB&Endi »**&f "
        private fun parser(input: String): String {
            var output = input
            for (color in colors) {
                output = output.replace(color.key, color.value)
            }
            return output
        }
        fun md(input: String): Component {
            return MineDown(parser(input)).toComponent()
        }

        fun mdUntrusted(input: String, untrustedString: String): Component {
            return MineDown(parser(input)).replace("<untrusted>", untrustedString).toComponent()
        }

        fun pre(input: String): Component {
            return MineDown(prefix + parser(input)).toComponent()
        }

        fun breakText(input: String, maxCharactersPerLine: Int): List<String> {
            val words = input.split(" ")
            val lines = mutableListOf<String>()

            var currentLine = ""
            for (word in words) {
                currentLine = if (currentLine.isEmpty()) {
                    word
                } else {
                    val potentialLine = "$currentLine $word"
                    if (potentialLine.length <= maxCharactersPerLine) {
                        potentialLine
                    } else {
                        lines.add(currentLine)
                        word
                    }
                }
            }

            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
            }

            return lines
        }

        fun mdlegacy(input: String): String {
            val component = MineDown(parser(input)).toComponent()
            return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(component)
        }
    }
}