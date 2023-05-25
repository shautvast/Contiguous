package nl.sanderhautvast.contiguous;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NestedBean {
    private StringBean stringBean;
    private IntBean intBean;
}
