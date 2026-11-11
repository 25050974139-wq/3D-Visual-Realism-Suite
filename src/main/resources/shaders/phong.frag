#version 330 core
out vec4 FragColor;

in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoords;

uniform vec3 lightPos;
uniform vec3 viewPos;
uniform vec3 lightColor;
uniform vec3 objectColor;
uniform int useBlinnPhong;

// Sampler Tekstur dari OpenGL
uniform sampler2D texture1;

void main() {
    // 1. Ambil sampel warna dari tekstur gambar
    vec4 texColor = texture(texture1, TexCoords);
    
    // Jika tekstur valid, gunakan warna tekstur. Jika tidak, gunakan objectColor
    vec3 baseColor = (texColor.rgb != vec3(0.0)) ? texColor.rgb : objectColor;

    // 2. Ambient Light (Cahaya Lingkungan)
    float ambientStrength = 0.3;
    vec3 ambient = ambientStrength * lightColor;
  
    // 3. Diffuse Light (Pencahayaan Utama)
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(lightPos - FragPos);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;
    
    // 4. Specular Light (Pantulan Kilau: Blinn-Phong vs Phong)
    vec3 viewDir = normalize(viewPos - FragPos);
    float spec = 0.0;
    
    if (useBlinnPhong == 1) {
        // Blinn-Phong Shading (Halfway Vector)
        vec3 halfwayDir = normalize(lightDir + viewDir);
        spec = pow(max(dot(norm, halfwayDir), 0.0), 32.0);
    } else {
        // Phong Shading (Reflection Vector)
        vec3 reflectDir = reflect(-lightDir, norm);
        spec = pow(max(dot(viewDir, reflectDir), 0.0), 8.0);
    }
    
    vec3 specular = 0.4 * spec * lightColor;

    // 5. Gabungkan pencahayaan dengan warna dasar/tekstur
    vec3 result = (ambient + diffuse + specular) * baseColor;
    FragColor = vec4(result, 1.0);
}