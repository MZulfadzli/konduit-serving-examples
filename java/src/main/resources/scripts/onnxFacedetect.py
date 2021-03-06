import os
import numpy as np
from PIL import Image
import torchvision.transforms as transforms
import onnxruntime

if os.path.isdir("java/src/main/resources/data/facedetector"):
    dl_path = os.path.abspath("java/src/main/resources/data/facedetector/facedetector.onnx")
else:
    dl_path = os.path.abspath("./src/main/resources/data/facedetector/facedetector.onnx")

sys.path.append(dl_path)
a,b,c,d=inputimage.shape
inputimage=inputimage.reshape(b,c,d)
im=np.array(inputimage)
image = Image.fromarray(im.astype('uint8'), 'RGB')
resize = transforms.Resize([240, 320])
img_y = resize(image)
to_tensor = transforms.ToTensor()
img_y = to_tensor(img_y)
img_y.unsqueeze_(0)

def to_numpy(tensor):
    return tensor.detach().cpu().numpy() if tensor.requires_grad else tensor.cpu().numpy()

ort_session = onnxruntime.InferenceSession(dl_path)
ort_inputs = {ort_session.get_inputs()[0].name: to_numpy(img_y)}
ort_outs = ort_session.run(None, ort_inputs)
_, boxes = ort_outs

print(boxes)
