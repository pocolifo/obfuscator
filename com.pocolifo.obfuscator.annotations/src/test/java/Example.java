
import com.pocolifo.obfuscator.annotations.Pass;
import com.pocolifo.obfuscator.annotations.PassOption;

@Pass(value = "GarbageMembersPass", options = {
        @PassOption(key = "addFields", value = "false"),
        @PassOption(key = "addMethods", value = "true")
})
@Pass(value = "RemapNamesPass", options = {
        @PassOption(key = "remapClassNames", value = "false"),
        @PassOption(key = "excludedMethods", value = {
                "main ([Ljava/lang/String;)V",
                "<init>",
                "<clinit>"
        })
})
public class Example {
}
