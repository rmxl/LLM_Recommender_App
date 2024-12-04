# LLM Recommender App

## Steps to Build the App

1. **Build the Project**
   - Open this project in **Android Studio**.
   - Build the project.
     
2. **Download and Use the Model**
   - To use a sample model, download **Gemma2** from the following link:
     ```
     https://www.kaggle.com/models/google/gemma-2/tfLite
     ```

3. **Specify the Model Paths**
   - Open the file `InferenceModel.kt`.
   - Execute the following command to push the model file to the device:
     ```
     adb push "model/path/on/local/machine" "path/to/your/model/on/device"
     ```
   - Specify the paths to your LLM stored on the device:
     ```kotlin
     // Example: Set the path to your model
     private const val MODEL_PATH1 = "path/to/your/model/on/device"
     ```
   - Remove any unnecessary model paths if not needed.
   - Change the corresponding options in `strings.xml` to change to the correct model name. The model path numbers are in the same order as in the XML file.


# Link to Demo Video

```
https://drive.google.com/file/d/1o0HGi_P9GRoDeSc0MW2YaXK94V5AfYVD/view?usp=sharing
```

# Demo GIF  

Here is a demo of the feature:  

![Demo of the Feature](./demo.gif)
