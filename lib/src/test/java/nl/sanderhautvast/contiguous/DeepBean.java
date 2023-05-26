package nl.sanderhautvast.contiguous;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeepBean {
    private NestedBean nestedBean;
    private Long ageOfMagrathea;
    private StringBean stringBean;
}
