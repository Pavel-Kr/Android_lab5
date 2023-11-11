precision mediump float;

uniform sampler2D texSampler;
varying vec2 vTexCoord;

uniform float sysTime;

const float PI = 3.1415926535897932;

//speed
const float speed = 0.01;
const float speed_x = 0.015;
const float speed_y = 0.015;

// refraction
const float emboss = 0.40;
const float intensity = 2.4;
const int steps = 10;
const float frequency = 5.0;
const int angle = 7; // better when a prime

// reflection
const float delta = 60.;
const float gain = 1500.;
const float reflectionCutOff = 0.012;
const float reflectionIntensity = 200000.;

float col(vec2 coord,float time)
{
    float delta_theta = 2.0 * PI / float(angle);
    float col = 0.0;
    float theta = 0.0;
    for (int i = 0; i < steps; i++)
    {
        vec2 adjc = coord;
        theta = delta_theta*float(i);
        adjc.x += cos(theta)*time*speed + time * speed_x;
        adjc.y -= sin(theta)*time*speed - time * speed_y;
        col = col + cos( (adjc.x*cos(theta) - adjc.y*sin(theta))*frequency)*intensity;
    }

    return cos(col);
}

void main() {
    //float time = iTime*1.3;
    float time = sysTime*1.3;

    //vec2 p = fragCoord.xy, c1 = p, c2 = p;
    vec2 p = vTexCoord.xy, c1 = p, c2 = p;
    float cc1 = col(c1,time);

    c2.x += 1.0/delta;
    float dx = emboss*(cc1-col(c2,time))/delta;

    c2.x = p.x;
    c2.y += 1.0/delta;
    float dy = emboss*(cc1-col(c2,time))/delta;

    c1.x += dx*2.;
    c1.y = -(c1.y+dy*2.);

    float alpha = 1.+dot(dx,dy)*gain;

    float ddx = dx - reflectionCutOff;
    float ddy = dy - reflectionCutOff;
    if (ddx > 0. && ddy > 0.)
    alpha = pow(alpha, ddx*ddy*reflectionIntensity);

    //vec4 col = texture(iChannel0,c1)*(alpha);
    vec4 col = texture2D(texSampler,c1)*(alpha);
    //fragColor = col;
    gl_FragColor = col;

    //gl_FragColor = texture2D(texSampler, vTexCoord);
}
