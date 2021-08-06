import kotlinx.browser.document
import org.w3c.dom.HTMLElement

fun createHtmlElement(tagName: String): HTMLElement =
    document.createElement(tagName) as HTMLElement
