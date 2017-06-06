package fr.minibilles.spinner

import org.w3c.dom.Element
import kotlin.browser.document
import kotlin.dom.appendText
import kotlin.js.Math

fun newSvgElement(name: String): Element {
    return document.createElementNS("http://www.w3.org/2000/svg", name);
}

fun circle(
    cx: Number, cy: Number, r: Number,
    stroke: String = "black", fill: String = "transparent",
    clipPath: String? = null
): Element {
    val circle = newSvgElement("circle");
    circle.setAttribute("cx", "${cx}");
    circle.setAttribute("cy", "${cy}");
    circle.setAttribute("r", "${r}");
    circle.setAttribute("stroke-width", "0.5");
    circle.setAttribute("stroke", stroke);
    circle.setAttribute("fill", fill);
    if (clipPath != null) {
        circle.setAttribute("clip-path", "url(#${clipPath})");
    }
    return circle;
}

fun text(
    x: Number, y: Number, text: String
): Element {
    val result = newSvgElement("text");
    result.setAttribute("x", "${x}");
    result.setAttribute("y", "${y}");
    result.setAttribute("font-size", "6");
    result.appendText(text);
    return result;
}

fun line(
    x1: Number, y1: Number, x2: Number, y2: Number, stroke: String = "black"
): Element {
    val line = newSvgElement("line");
    line.setAttribute("x1", "${x1}");
    line.setAttribute("y1", "${y1}");
    line.setAttribute("x2", "${x2}");
    line.setAttribute("y2", "${y2}");
    line.setAttribute("stroke", stroke);
    line.setAttribute("stroke-width", "0.5");
    return line;
}

fun measure(x: Int = 10, y: Int = 10, value: Int = 10): Element {
    val g = newSvgElement("g");
    g.appendChild(text(x, y-3, "${value}mm"));
    g.appendChild(line(x, y, x+value, y));
    g.appendChild(line(x, y-2, x, y+2));
    g.appendChild(line(x+value, y-2, x+value, y+2));
    return g;
}

fun path(path: Path, stroke : String = "black", fill: String = "transparent"): Element {
    val result = newSvgElement("path")
    result.setAttribute("d", path.result)
    result.setAttribute("stroke", stroke)
    result.setAttribute("fill", fill)
    result.setAttribute("stroke-width", "0.5");
    return result;
}

fun clear(element: Element) {
    var child = element.firstChild
    while (child != null) {
        element.removeChild(child);
        child = element.firstChild;
    }
}

class Path() {

    private val path = StringBuilder();

    private fun spacer() {
        if (path.length > 0) {
            path.append(" ")
        }
    }

    fun moveTo(x: Number, y: Number) {
        spacer()
        path.append("M ${x} ${y}")
    }

    fun moveDelta(dx: Number, dy: Number) {
        spacer()
        path.append("m ${dx} ${dy}")
    }

    fun lineTo(x: Number, y: Number) {
        spacer()
        path.append("L ${x} ${y}")
    }

    fun lineDelta(dx: Number, dy: Number) {
        spacer()
        path.append("l ${dx} ${dy}")
    }

    fun horizontalTo(x: Number) {
        spacer()
        path.append("H ${x}")
    }

    fun horizontalDelta(dx: Number) {
        spacer()
        path.append("h ${dx}")
    }

    fun verticalTo(y: Number) {
        spacer()
        path.append("V ${y}")
    }

    fun verticalDelta(dy: Number) {
        spacer()
        path.append("v ${dy}")
    }

    fun splineTo(x2: Number, y2: Number, x: Number, y: Number) {
        spacer()
        path.append("S ${x2} ${y2} ${x} ${y}")
    }

    fun quadraticTo(x1: Number, y1: Number, x: Number, y: Number) {
        spacer()
        path.append("S ${x1} ${y1} ${x} ${y}")
    }

    fun close() {
        spacer()
        path.append("Z")
    }

    val result: String
        get() = path.toString()

}

@JsName("schematic")
fun schematic(
    branchCount: Int = 3,
    internalRadius: Int = 30,
    bearingSize: Int = 18
) {
    // radius in mm
    val bearingRadius = bearingSize / 2;
    val spinnerRadius = internalRadius + bearingRadius * 1.5;

    // center coordinates in mm
    val cx = spinnerRadius + 10;
    val cy = cx;

    val svg = document.getElementById("spinner");
    if (svg != null) {
        clear(svg);


        val defs = newSvgElement("defs");
        val clipPath = newSvgElement("clipPath");
        clipPath.id = "clip";
        clipPath.appendChild(circle(cx, cy, spinnerRadius+1));
        defs.appendChild(clipPath);
        svg.appendChild(defs);

        svg.appendChild(circle(cx, cy, bearingRadius, "black", "blue"));
        svg.appendChild(circle(cx, cy, 1, "black", "black"));

        // draws circle for ball bearings
        for (i in 0..branchCount - 1) {
            val angleStep = 2 * Math.PI / branchCount
            val angle = angleStep * i;
            val cix = cx + internalRadius * Math.cos(angle);
            val ciy = cy + internalRadius * Math.sin(angle);

            svg.appendChild(circle(cix, ciy, bearingRadius));
            svg.appendChild(circle(cix, ciy, 1, "black", "black"));
        }

        // draws path for
        val bounds = Path();
        for (i in 0..branchCount - 1) {
            val angleStep = 2 * Math.PI / branchCount
            val angle = angleStep * i;

            val previousQuarterAngle = angle - angleStep / 6
            val quarterAngle = angle + angleStep / 6
            val semiAngle = angle + angleStep / 2

            val x = cx + spinnerRadius * Math.cos(angle);
            val y = cy + spinnerRadius * Math.sin(angle);

            val controlX = cx + spinnerRadius * Math.cos(previousQuarterAngle);
            val controlY = cy + spinnerRadius * Math.sin(previousQuarterAngle);

            val nextX = cx + spinnerRadius/3 * Math.cos(semiAngle);
            val nextY = cy + spinnerRadius/3 * Math.sin(semiAngle);

            val nextControlX = cx + spinnerRadius * Math.cos(quarterAngle);
            val nextControlY = cy + spinnerRadius * Math.sin(quarterAngle);

            if (i == 0) {
                bounds.moveTo(controlX, controlY)
                bounds.lineTo(x, y)
            } else {
                bounds.lineTo(controlX, controlY)
                bounds.lineTo(x, y)
            }
            bounds.lineTo(nextControlX, nextControlY)
            bounds.lineTo(nextX, nextY)

        }
        bounds.close()
        svg.appendChild(path(bounds))

        svg.appendChild(measure())
    }
}

fun main(args: Array<String>) {
    println("Started");
}
