package languagetool_nlp


import grails.rest.*
import grails.converters.*

//import io.swagger.annotations.*
import com.wordnik.swagger.annotations.*

@Api(value = "Tokenization services", 
    description = "Tokenization services for Ukrainian language",
    produces = 'application/json',
    consumes = 'application/json'
)
class TokenizeController {
    static responseFormats = ['json']
    static allowedMethods = [save: "POST"]
    static defaultAction = "save"
    static TEXT_LIMIT = 1000

    def tokenizeService

    @ApiOperation(value = "Tokenizes the text into sentences and then into words", 
                httpMethod = "POST"
                ,
                response = Response.class)
    @ApiResponses([
        @ApiResponse(code = 400, message = "Invalid body provided"),
        @ApiResponse(code = 400, message = "Text limit exceeded")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(name = 'body', paramType = 'body', required = true, dataType='InputBody', 
            value='Body text; e.g<br>{"text": "І.А. Іванов прийшов додому. І з\'їв 2 тис. кавунів."}')
    ])
    def save() {
        if( ! request.JSON?.text ) {
            render(status: 400, text: "\"text\" field not specified in the request")
            return
        }

        if( request.JSON.text.size() > TEXT_LIMIT ) {
            render(status: 400, text: String.format("text length cannot exceed %d characters", TEXT_LIMIT))
            return
        }

        try {
            def tokens = tokenizeService.tokenize(request.JSON, params)

            def response = new Response(tokens: tokens)

            if( testLatCyrMix(request.JSON.text) ) {
                response.notes = "Text contains mix of Cyrillic and Lating which may produce suboptimal results"
            }

            render response as JSON
        }
        catch(Exception e) {
            e.printStackTrace()
            render(status: 500, text: "Internal error: " + e.getMessage())
            return
        }

    }

    def testLatCyrMix(text) {
        return text =~ /[а-яіїєґА-ЯІЇЄҐ]['’ʼ-]?[a-zA-Z]|[a-zA-Z]['’ʼ-]?[а-яіїєґА-ЯІЇЄҐ]/
    }


    static class Response {
        List<String> tokens
        String notes
    }
}
